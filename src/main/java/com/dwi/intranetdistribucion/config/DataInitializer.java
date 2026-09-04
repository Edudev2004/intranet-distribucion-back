package com.dwi.intranetdistribucion.config;

import com.dwi.intranetdistribucion.model.Permiso;
import com.dwi.intranetdistribucion.model.Rol;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.PermisoRepository;
import com.dwi.intranetdistribucion.repository.RolRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Inicializar Permisos por Módulo
        crearPermisoSiNoExiste("USUARIOS_VER", "Ver módulo de usuarios", "USUARIOS");
        crearPermisoSiNoExiste("USUARIOS_CREAR", "Crear y editar usuarios", "USUARIOS");
        crearPermisoSiNoExiste("ROLES_GESTIONAR", "Gestionar roles y permisos", "SEGURIDAD");
        crearPermisoSiNoExiste("INVENTARIO_VER", "Ver stock e inventario", "INVENTARIO");
        crearPermisoSiNoExiste("INVENTARIO_GESTIONAR", "Crear productos y ajustar stock", "INVENTARIO");
        crearPermisoSiNoExiste("PEDIDOS_CREAR", "Generar nuevos pedidos", "PEDIDOS");
        crearPermisoSiNoExiste("PEDIDOS_APROBAR", "Aprobar o rechazar pedidos", "PEDIDOS");
        crearPermisoSiNoExiste("LOGISTICA_RUTAS", "Gestionar zonas y rutas de distribución", "LOGISTICA");
        crearPermisoSiNoExiste("FLOTA_GESTIONAR", "Gestionar vehículos y conductores", "FLOTA");
        crearPermisoSiNoExiste("REPORTES_VER", "Acceder a reportes y dashboards", "REPORTES");

        // 2. Inicializar Rol ADMIN con todos los permisos
        Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
            Set<Permiso> todosLosPermisos = new HashSet<>(permisoRepository.findAll());
            Rol nuevoRol = Rol.builder()
                    .nombre("ADMIN")
                    .descripcion("Administrador Total del Sistema")
                    .permisos(todosLosPermisos)
                    .build();
            return rolRepository.save(nuevoRol);
        });

        // 3. Inicializar roles base del sistema
        crearRolSiNoExiste("ALMACENERO", "Gestión de inventario y despacho", List.of("INVENTARIO_VER", "INVENTARIO_GESTIONAR", "PEDIDOS_APROBAR"));
        crearRolSiNoExiste("CONDUCTOR", "Acceso a rutas asignadas e incidencias", List.of("LOGISTICA_RUTAS"));
        crearRolSiNoExiste("CLIENTE", "Generación y consulta de pedidos", List.of("PEDIDOS_CREAR"));

        // 4. Inicializar Usuario Admin Inicial
        String adminEmail = "admin@distribucion.com";
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin"))
                    .esActivo(true)
                    .intentosFallidos(0)
                    .esBloqueado(false)
                    .rol(rolAdmin)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("=================================================");
            System.out.println(">>> USUARIO ADMINISTRADOR INICIAL CREADO EN BD <<<");
            System.out.println(">>> Email:    " + adminEmail);
            System.out.println(">>> Password: admin");
            System.out.println(">>> Rol:      ADMIN");
            System.out.println("=================================================");
        }
    }

    private void crearPermisoSiNoExiste(String nombre, String descripcion, String modulo) {
        if (!permisoRepository.existsByNombre(nombre)) {
            permisoRepository.save(Permiso.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .modulo(modulo)
                    .build());
        }
    }

    private void crearRolSiNoExiste(String nombre, String descripcion, List<String> nombresPermisos) {
        if (!rolRepository.existsByNombre(nombre)) {
            Set<Permiso> permisos = new HashSet<>();
            for (String nombrePermiso : nombresPermisos) {
                permisoRepository.findByNombre(nombrePermiso).ifPresent(permisos::add);
            }
            rolRepository.save(Rol.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .permisos(permisos)
                    .build());
        }
    }
}
