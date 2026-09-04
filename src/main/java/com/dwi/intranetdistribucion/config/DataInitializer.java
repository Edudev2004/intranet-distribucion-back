package com.dwi.intranetdistribucion.config;

import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@distribucion.com";
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin"))
                    .esActivo(true)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("=================================================");
            System.out.println(">>> USUARIO ADMINISTRADOR INICIAL CREADO EN BD <<<");
            System.out.println(">>> Email:    " + adminEmail);
            System.out.println(">>> Password: admin");
            System.out.println("=================================================");
        }
    }
}
