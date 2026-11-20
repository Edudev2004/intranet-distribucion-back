package com.dwi.intranetdistribucion.config;

import com.dwi.intranetdistribucion.model.*;
import com.dwi.intranetdistribucion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final TipoIncidenciaRepository tipoIncidenciaRepository;
    private final EstadoEntregaRepository estadoEntregaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
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
        crearPermisoSiNoExiste("SEDES", "VER");
        crearPermisoSiNoExiste("SEDES", "GESTIONAR");
        crearPermisoSiNoExiste("Entregas", "ver");
        crearPermisoSiNoExiste("Entregas", "actualizar");
        crearPermisoSiNoExiste("INCIDENCIAS", "VER");
        crearPermisoSiNoExiste("INCIDENCIAS", "REGISTRAR");

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
        crearRolSiNoExiste("SUPERVISOR", "Supervisor de operaciones y aprobación de pedidos", List.of("PEDIDOS_VER", "PEDIDOS_APROBAR"));
        crearRolSiNoExiste("ALMACENERO", "Gestión de inventario y despacho", List.of("INVENTARIO_VER", "INVENTARIO_GESTIONAR", "PEDIDOS_APROBAR"));
        crearRolSiNoExiste("CONDUCTOR", "Acceso a rutas asignadas e incidencias", List.of("LOGISTICA_RUTAS"));
        crearRolSiNoExiste("CLIENTE", "Generación y consulta de pedidos", List.of("PEDIDOS_CREAR"));

        // 5. Inicializar Usuarios Demo del Sistema para todos los roles
        crearUsuarioDemoSiNoExiste("admin@distribucion.com", "admin", "admin", "Administrador", "Sistema", "ADMIN", sedeDefault);
        crearUsuarioDemoSiNoExiste("supervisor@distribucion.com", "admin", "supervisor", "Carlos", "Supervisor", "SUPERVISOR", sedeDefault);
        crearUsuarioDemoSiNoExiste("almacenero@distribucion.com", "admin", "almacenero", "Jorge", "Almacenero", "ALMACENERO", sedeDefault);
        crearUsuarioDemoSiNoExiste("conductor@distribucion.com", "admin", "conductor", "Marcos", "Conductor", "CONDUCTOR", sedeDefault);
        crearUsuarioDemoSiNoExiste("cliente@distribucion.com", "admin", "cliente", "Comercial", "Cliente", "CLIENTE", sedeDefault);

        // 6. Inicializar Tipos de Movimiento de Inventario
        crearTipoMovimientoSiNoExiste("ENTRADA_COMPRA", 'E');
        crearTipoMovimientoSiNoExiste("ENTRADA_AJUSTE", 'E');
        crearTipoMovimientoSiNoExiste("ENTRADA_DEVOLUCION", 'E');
        crearTipoMovimientoSiNoExiste("SALIDA_VENTA", 'S');
        crearTipoMovimientoSiNoExiste("SALIDA_MERMA", 'S');
        crearTipoMovimientoSiNoExiste("SALIDA_AJUSTE", 'S');
        crearTipoMovimientoSiNoExiste("SALIDA_TRASLADO", 'S');

        // 7. Inicializar Estados de Entrega (RF-16)
        crearEstadoEntregaSiNoExiste((short) 1, "Pendiente");
        crearEstadoEntregaSiNoExiste((short) 2, "En Ruta");
        crearEstadoEntregaSiNoExiste((short) 3, "Entregado");
        crearEstadoEntregaSiNoExiste((short) 4, "No Entregado");

        // 8. Inicializar Tipos de Incidencia (RF-17)
        crearTipoIncidenciaSiNoExiste("Cliente Ausente / Local Cerrado");
        crearTipoIncidenciaSiNoExiste("Dirección Errónea / No Ubicada");
        crearTipoIncidenciaSiNoExiste("Pedido Rechazado por Cliente");
        crearTipoIncidenciaSiNoExiste("Mercadería Dañada / Averiada");
        crearTipoIncidenciaSiNoExiste("Problema Vehicular / Mecánico");
        crearTipoIncidenciaSiNoExiste("Otro Motivo");

        // 9. Inicializar Tipos de Documento para Clientes (RF-09)
        crearTipoDocumentoSiNoExiste("DNI");
        crearTipoDocumentoSiNoExiste("RUC");
        crearTipoDocumentoSiNoExiste("Carnet de Extranjería");
        crearTipoDocumentoSiNoExiste("Pasaporte");
    }

    private void crearTipoDocumentoSiNoExiste(String nombre) {
        if (tipoDocumentoRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            tipoDocumentoRepository.save(TipoDocumento.builder()
                    .nombre(nombre)
                    .build());
        }
    }

    private void crearEstadoEntregaSiNoExiste(Short id, String nombre) {
        if (!estadoEntregaRepository.existsById(id)) {
            estadoEntregaRepository.save(EstadoEntrega.builder()
                    .idEstadoEntrega(id)
                    .nombre(nombre)
                    .build());
        }
    }

    private void crearTipoIncidenciaSiNoExiste(String nombre) {
        if (!tipoIncidenciaRepository.existsByNombreIgnoreCase(nombre)) {
            tipoIncidenciaRepository.save(TipoIncidencia.builder()
                    .nombre(nombre)
                    .build());
        }
    }

    private void crearTipoMovimientoSiNoExiste(String nombre, Character naturaleza) {
        if (!tipoMovimientoRepository.existsByNombreIgnoreCase(nombre)) {
            tipoMovimientoRepository.save(TipoMovimiento.builder()
                    .nombre(nombre)
                    .naturaleza(naturaleza)
                    .build());
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

    private void crearUsuarioDemoSiNoExiste(String correo, String password, String username, String nombres, String apellidos, String nombreRol, Sede sede) {
        if (!usuarioRepository.existsByCorreoIgnoreCase(correo) && !usuarioRepository.existsByUsernameIgnoreCase(username)) {
            Rol rol = rolRepository.findByNombre(nombreRol).orElse(null);
            Usuario usuario = Usuario.builder()
                    .nombres(nombres)
                    .apellidos(apellidos)
                    .username(username)
                    .correo(correo)
                    .passwordHash(passwordEncoder.encode(password))
                    .sede(sede)
                    .activo(true)
                    .intentosFallidos((short) 0)
                    .bloqueado(false)
                    .rol(rol)
                    .build();

            usuarioRepository.save(usuario);
            System.out.println(">>> Usuario Demo Inicial Creado: " + correo + " (Rol: " + nombreRol + ") <<<");
        } else {
            usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(u -> {
                if (Boolean.TRUE.equals(u.getEsBloqueado()) || (u.getIntentosFallidos() != null && u.getIntentosFallidos() > 0)) {
                    u.setBloqueado(false);
                    u.setIntentosFallidos((short) 0);
                    u.setFechaBloqueo(null);
                    usuarioRepository.save(u);
                }
            });
        }
    }
}
