package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.config.security.JwtTokenProvider;
import com.dwi.intranetdistribucion.dto.request.LoginRequestDTO;
import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.JwtAuthResponseDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;
import com.dwi.intranetdistribucion.model.Rol;
import com.dwi.intranetdistribucion.model.Sede;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.RolRepository;
import com.dwi.intranetdistribucion.repository.SedeRepository;
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
    private final RolRepository rolRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(noRollbackFor = {BadCredentialsException.class, LockedException.class})
    public JwtAuthResponseDTO login(LoginRequestDTO loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (Boolean.TRUE.equals(usuario.getEsBloqueado())) {
                if (usuario.getFechaBloqueo() != null &&
                        usuario.getFechaBloqueo().plusMinutes(15).isBefore(java.time.LocalDateTime.now())) {
                    // Han pasado más de 15 minutos: desbloqueo automático
                    usuarioRepository.desbloquearUsuario(usuario.getId());
                    usuario.setEsBloqueado(false);
                    usuario.setIntentosFallidos((short) 0);
                } else {
                    long minutosRestantes = 15;
                    if (usuario.getFechaBloqueo() != null) {
                        long minutosTranscurridos = java.time.Duration.between(
                                usuario.getFechaBloqueo(),
                                java.time.LocalDateTime.now()
                        ).toMinutes();
                        minutosRestantes = Math.max(1, 15 - minutosTranscurridos);
                    }
                    throw new LockedException("La cuenta se encuentra bloqueada por superar los 5 intentos fallidos. Vuelve a intentarlo en " + minutosRestantes + " minuto(s).");
                }
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
        String correo = request.getCorreo() != null ? request.getCorreo().trim().toLowerCase() : "";
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("El email ya se encuentra registrado: " + correo);
        }

        Rol rol = (request.getIdRol() != null)
                ? rolRepository.findById(request.getIdRol().shortValue()).orElseGet(() -> rolRepository.findByNombre("ADMIN").orElse(null))
                : rolRepository.findByNombre("ADMIN").orElse(null);

        Sede sede = (request.getIdSede() != null)
                ? sedeRepository.findById(request.getIdSede()).orElseGet(() -> sedeRepository.findAll().stream().findFirst().orElse(null))
                : sedeRepository.findAll().stream().findFirst().orElse(null);

        Usuario usuario = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .username(request.getUsername())
                .correo(correo)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .sede(sede)
                .activo(true)
                .intentosFallidos((short) 0)
                .bloqueado(false)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return UsuarioResponseDTO.builder()
                .idUsuario(guardado.getIdUsuario())
                .nombres(guardado.getNombres())
                .apellidos(guardado.getApellidos())
                .username(guardado.getUsername())
                .correo(guardado.getCorreo())
                .activo(guardado.getActivo())
                .intentosFallidos(guardado.getIntentosFallidos())
                .bloqueado(guardado.getBloqueado())
                .creadoEn(guardado.getCreadoEn())
                .build();
    }
}
