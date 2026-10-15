package com.dwi.intranetdistribucion.config;

import com.dwi.intranetdistribucion.model.*;
import com.dwi.intranetdistribucion.repository.*;
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
    private final ModuloRepository moduloRepository;
    private final AccionRepository accionRepository;
    private final PermisoRepository permisoRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Inicializar Sede Central por defecto si no existe
        Sede sedeDefault = sedeRepository.findAll().stream().findFirst().orElseGet(() ->
                sedeRepository.save(Sede.builder()
                        .nombre("Sede Central")
                        .direccion("Av. Principal 123, Lima")
                        .activo(true)
                        .build())
        );

        // 2. Inicializar Permisos (Modulo + Accion)
        crearPermisoSiNoExiste("USUARIOS", "VER");
        crearPermisoSiNoExiste("USUARIOS", "CREAR");
        crearPermisoSiNoExiste("SEGURIDAD", "ROLES_GESTIONAR");
        crearPermisoSiNoExiste("INVENTARIO", "VER");
        crearPermisoSiNoExiste("INVENTARIO", "GESTIONAR");
        crearPermisoSiNoExiste("PEDIDOS", "CREAR");
        crearPermisoSiNoExiste("PEDIDOS", "APROBAR");
        crearPermisoSiNoExiste("LOGISTICA", "RUTAS");
        crearPermisoSiNoExiste("FLOTA", "GESTIONAR");
        crearPermisoSiNoExiste("REPORTES", "VER");

        // 3. Inicializar Rol ADMIN con todos los permisos
        Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
            Set<Permiso> todosLosPermisos = new HashSet<>(permisoRepository.findAll());
            Rol nuevoRol = Rol.builder()
                    .nombre("ADMIN")
                    .descripcion("Administrador Total del Sistema")
                    .permisos(todosLosPermisos)
                    .build();
            return rolRepository.save(nuevoRol);
        });

        // Asegurar que el rol ADMIN tenga todos los permisos actualizados
        if (rolAdmin.getPermisos() == null || rolAdmin.getPermisos().size() < permisoRepository.count()) {
            rolAdmin.setPermisos(new HashSet<>(permisoRepository.findAll()));
            rolRepository.save(rolAdmin);
        }

        // 4. Inicializar roles base del sistema
        crearRolSiNoExiste("ALMACENERO", "Gestión de inventario y despacho", List.of("INVENTARIO_VER", "INVENTARIO_GESTIONAR", "PEDIDOS_APROBAR"));
        crearRolSiNoExiste("CONDUCTOR", "Acceso a rutas asignadas e incidencias", List.of("LOGISTICA_RUTAS"));
        crearRolSiNoExiste("CLIENTE", "Generación y consulta de pedidos", List.of("PEDIDOS_CREAR"));

        // 5. Inicializar Usuario Admin Inicial
        String adminEmail = "admin@distribucion.com";
        if (!usuarioRepository.existsByCorreoIgnoreCase(adminEmail)) {
            Usuario admin = Usuario.builder()
                    .nombres("Administrador")
                    .apellidos("Sistema")
                    .username("admin")
                    .correo(adminEmail)
                    .passwordHash(passwordEncoder.encode("admin"))
                    .sede(sedeDefault)
                    .activo(true)
                    .intentosFallidos((short) 0)
                    .bloqueado(false)
                    .rol(rolAdmin)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("=================================================");
            System.out.println(">>> USUARIO ADMINISTRADOR INICIAL CREADO EN BD <<<");
            System.out.println(">>> Email:    " + adminEmail);
            System.out.println(">>> Password: admin");
            System.out.println(">>> Rol:      ADMIN");
            System.out.println(">>> Sede:     " + sedeDefault.getNombre());
            System.out.println("=================================================");
        }
    }

    private void crearPermisoSiNoExiste(String nombreModulo, String nombreAccion) {
        Modulo modulo = moduloRepository.findByNombre(nombreModulo).orElseGet(() ->
                moduloRepository.save(Modulo.builder().nombre(nombreModulo).build())
        );

        Accion accion = accionRepository.findByNombre(nombreAccion).orElseGet(() ->
                accionRepository.save(Accion.builder().nombre(nombreAccion).build())
        );

        if (permisoRepository.findByModulo_NombreAndAccion_Nombre(nombreModulo, nombreAccion).isEmpty()) {
            permisoRepository.save(Permiso.builder()
                    .modulo(modulo)
                    .accion(accion)
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
