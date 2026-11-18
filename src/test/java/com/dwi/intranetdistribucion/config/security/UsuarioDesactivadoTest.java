package com.dwi.intranetdistribucion.config.security;

import com.dwi.intranetdistribucion.controller.UsuarioController;
import com.dwi.intranetdistribucion.exception.GlobalExceptionHandler;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioDesactivadoTest {

    private static final String CORREO = "ana@distribucion.com";
    private static final String CLAVE = "Segura123";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UsuarioService usuarioService;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(usuarioRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("El usuario activo queda habilitado y el inactivo no")
    void reflejaCampoActivo() {
        when(usuarioRepository.findByEmail(CORREO)).thenReturn(Optional.of(usuario(true)));
        assertTrue(userDetailsService.loadUserByUsername(CORREO).isEnabled());

        when(usuarioRepository.findByEmail(CORREO)).thenReturn(Optional.of(usuario(false)));
        assertFalse(userDetailsService.loadUserByUsername(CORREO).isEnabled());
    }

    @Test
    @DisplayName("El login rechaza a un usuario desactivado con DisabledException")
    void loginRechazaUsuarioDesactivado() {
        when(usuarioRepository.findByEmail(CORREO)).thenReturn(Optional.of(usuario(false)));
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(4));

        assertThrows(DisabledException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(CORREO, CLAVE)));
    }

    @Test
    @DisplayName("El filtro JWT no autentica a un usuario desactivado")
    void filtroNoAutenticaUsuarioDesactivado() throws Exception {
        when(usuarioRepository.findByEmail(CORREO)).thenReturn(Optional.of(usuario(false)));

        ejecutarFiltro();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("El filtro JWT autentica a un usuario activo")
    void filtroAutenticaUsuarioActivo() throws Exception {
        when(usuarioRepository.findByEmail(CORREO)).thenReturn(Optional.of(usuario(true)));

        ejecutarFiltro();

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(CORREO, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("Usuarios responde en /api/v1/usuarios y en /api/usuarios")
    void usuariosRespondeEnAmbasRutas() throws Exception {
        when(usuarioService.obtenerTodos()).thenReturn(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService)).build();

        mockMvc.perform(get("/api/v1/usuarios")).andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DisabledException responde 403 con mensaje claro")
    void disabledResponde403() throws Exception {
        when(usuarioService.obtenerTodos()).thenThrow(new DisabledException("User is disabled"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("La cuenta está desactivada. Contacte al administrador."));
    }

    private void ejecutarFiltro() throws Exception {
        when(jwtTokenProvider.validarToken("token")).thenReturn(true);
        when(jwtTokenProvider.obtenerUsernameDelJWT("token")).thenReturn(CORREO);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService)
                .doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    private Usuario usuario(boolean activo) {
        return Usuario.builder()
                .correo(CORREO)
                .passwordHash(new BCryptPasswordEncoder(4).encode(CLAVE))
                .activo(activo)
                .build();
    }
}
