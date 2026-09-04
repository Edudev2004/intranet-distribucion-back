package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.config.security.JwtTokenProvider;
import com.dwi.intranetdistribucion.dto.request.LoginRequestDTO;
import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.JwtAuthResponseDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(noRollbackFor = {BadCredentialsException.class, LockedException.class})
    public JwtAuthResponseDTO login(LoginRequestDTO loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (Boolean.TRUE.equals(usuario.getEsBloqueado())) {
                throw new LockedException("La cuenta se encuentra bloqueada por superar los 5 intentos fallidos.");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );

                // Si el login es exitoso, reiniciamos el contador
                usuarioRepository.reiniciarIntentosFallidos(usuario.getId());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtTokenProvider.generarToken(authentication);

                return JwtAuthResponseDTO.builder()
                        .accessToken(token)
                        .tokenType("Bearer")
                        .build();

            } catch (BadCredentialsException ex) {
                // Incrementamos los intentos fallidos en la base de datos de forma persistente
                usuarioRepository.incrementarIntentosFallidos(usuario.getId());

                int nuevosIntentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;

                if (nuevosIntentos >= 5) {
                    usuarioRepository.bloquearUsuario(usuario.getId());
                    throw new LockedException("Cuenta bloqueada. Ha superado los 5 intentos fallidos permitidos.");
                } else {
                    throw new BadCredentialsException("Credenciales incorrectas. Llevas " + nuevosIntentos + " de 5 intentos.");
                }
            }
        } else {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .esActivo(true)
                .intentosFallidos(0)
                .esBloqueado(false)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return UsuarioResponseDTO.builder()
                .id(guardado.getId())
                .nombre(guardado.getNombre())
                .apellido(guardado.getApellido())
                .email(guardado.getEmail())
                .esActivo(guardado.getEsActivo())
                .fechaCreacion(guardado.getFechaCreacion())
                .build();
    }
}
