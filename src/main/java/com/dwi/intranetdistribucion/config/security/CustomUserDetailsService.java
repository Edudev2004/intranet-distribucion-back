package com.dwi.intranetdistribucion.config.security;

import com.dwi.intranetdistribucion.model.Permiso;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();

        if (usuario.getRol() != null) {
            // Mapear nombre de Rol (ej. ROLE_ADMIN)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));

            // Mapear Permisos del Rol (ej. USUARIOS_VER)
            if (usuario.getRol().getPermisos() != null) {
                for (Permiso permiso : usuario.getRol().getPermisos()) {
                    authorities.add(new SimpleGrantedAuthority(permiso.getNombre()));
                }
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                authorities
        );
    }
}
