package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.SedeRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ResponsableSedeResponseDTO;
import com.dwi.intranetdistribucion.dto.response.SedeResponseDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceInUseException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Rol;
import com.dwi.intranetdistribucion.model.Sede;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.SedeRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.impl.SedeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SedeServiceTest {

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SedeServiceImpl sedeService;

    private Sede sedeEjemplo;
    private Usuario responsableEjemplo;

    @BeforeEach
    void setUp() {
        responsableEjemplo = Usuario.builder()
                .idUsuario(1)
                .nombres("Carlos")
                .apellidos("García")
                .username("cgarcia")
                .correo("cgarcia@distribucion.com")
                .activo(true)
                .bloqueado(false)
                .rol(Rol.builder().idRol((short) 1).nombre("ADMIN").build())
                .build();

        sedeEjemplo = Sede.builder()
                .idSede(10)
                .nombre("Sede Norte")
                .direccion("Av. Los Pinos 456, Chiclayo")
                .responsable(responsableEjemplo)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("obtenerTodas con soloActivos=false retorna todas las sedes con sus contadores")
    void obtenerTodas_TodasLasSedes() {
        when(sedeRepository.findAllWithResponsable()).thenReturn(List.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(3L);
        when(sedeRepository.contarAlmacenesAsociados(10)).thenReturn(2L);

        List<SedeResponseDTO> resultado = sedeService.obtenerTodas(false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Sede Norte");
        assertThat(resultado.get(0).getTotalUsuarios()).isEqualTo(3L);
        assertThat(resultado.get(0).getTotalAlmacenes()).isEqualTo(2L);
        assertThat(resultado.get(0).getNombreResponsable()).isEqualTo("Carlos García");
    }

    @Test
    @DisplayName("obtenerTodas con soloActivos=true retorna únicamente las sedes activas")
    void obtenerTodas_SoloActivas() {
        when(sedeRepository.findByActivoTrueWithResponsable()).thenReturn(List.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(1L);
        when(sedeRepository.contarAlmacenesAsociados(10)).thenReturn(1L);

        List<SedeResponseDTO> resultado = sedeService.obtenerTodas(true);

        assertThat(resultado).hasSize(1);
        verify(sedeRepository).findByActivoTrueWithResponsable();
    }

    @Test
    @DisplayName("obtenerPorId retorna detalle de sede si existe")
    void obtenerPorId_Existe() {
        when(sedeRepository.findByIdWithResponsable(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(5L);
        when(sedeRepository.contarAlmacenesAsociados(10)).thenReturn(2L);

        SedeResponseDTO resultado = sedeService.obtenerPorId(10);

        assertThat(resultado.getIdSede()).isEqualTo(10);
        assertThat(resultado.getNombre()).isEqualTo("Sede Norte");
        assertThat(resultado.getTotalUsuarios()).isEqualTo(5L);
    }

    @Test
    @DisplayName("obtenerPorId lanza ResourceNotFoundException si la sede no existe")
    void obtenerPorId_NoExiste_LanzaExcepcion() {
        when(sedeRepository.findByIdWithResponsable(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sedeService.obtenerPorId(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sede no encontrada con id: 99");
    }

    @Test
    @DisplayName("crear sede exitosamente con responsable")
    void crear_Exitoso() {
        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Sur")
                .direccion("Av. El Sol 789, Arequipa")
                .idResponsable(1)
                .activo(true)
                .build();

        when(sedeRepository.existsByNombreIgnoreCase("Sede Sur")).thenReturn(false);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(responsableEjemplo));
        when(sedeRepository.save(any(Sede.class))).thenAnswer(invocation -> {
            Sede guardada = invocation.getArgument(0);
            guardada.setIdSede(20);
            return guardada;
        });

        SedeResponseDTO resultado = sedeService.crear(request);

        assertThat(resultado.getIdSede()).isEqualTo(20);
        assertThat(resultado.getNombre()).isEqualTo("Sede Sur");
        assertThat(resultado.getIdResponsable()).isEqualTo(1);
        assertThat(resultado.getNombreResponsable()).isEqualTo("Carlos García");
        verify(sedeRepository).save(any(Sede.class));
    }

    @Test
    @DisplayName("crear sede lanza DuplicateResourceException si el nombre ya existe")
    void crear_NombreDuplicado_LanzaExcepcion() {
        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Norte")
                .direccion("Dirección X")
                .build();

        when(sedeRepository.existsByNombreIgnoreCase("Sede Norte")).thenReturn(true);

        assertThatThrownBy(() -> sedeService.crear(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe una sede con el nombre 'Sede Norte'");

        verify(sedeRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear sede lanza ResourceNotFoundException si el idResponsable no existe")
    void crear_ResponsableNoExiste_LanzaExcepcion() {
        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Este")
                .direccion("Dirección Y")
                .idResponsable(999)
                .build();

        when(sedeRepository.existsByNombreIgnoreCase("Sede Este")).thenReturn(false);
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sedeService.crear(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario responsable no encontrado con id: 999");
    }

    @Test
    @DisplayName("crear sede lanza BadRequestException si el responsable está inactivo")
    void crear_ResponsableInactivo_LanzaExcepcion() {
        responsableEjemplo.setActivo(false);

        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Este")
                .direccion("Dirección Y")
                .idResponsable(1)
                .build();

        when(sedeRepository.existsByNombreIgnoreCase("Sede Este")).thenReturn(false);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(responsableEjemplo));

        assertThatThrownBy(() -> sedeService.crear(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No se puede asignar como responsable a un usuario inactivo");
    }

    @Test
    @DisplayName("crear sede lanza BadRequestException si el responsable está bloqueado")
    void crear_ResponsableBloqueado_LanzaExcepcion() {
        responsableEjemplo.setBloqueado(true);

        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Este")
                .direccion("Dirección Y")
                .idResponsable(1)
                .build();

        when(sedeRepository.existsByNombreIgnoreCase("Sede Este")).thenReturn(false);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(responsableEjemplo));

        assertThatThrownBy(() -> sedeService.crear(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No se puede asignar como responsable a un usuario bloqueado");
    }

    @Test
    @DisplayName("actualizar sede exitosamente")
    void actualizar_Exitoso() {
        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Norte Modificada")
                .direccion("Nueva Direccion 123")
                .idResponsable(1)
                .activo(true)
                .build();

        when(sedeRepository.findById(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.existsByNombreIgnoreCaseAndIdSedeNot("Sede Norte Modificada", 10)).thenReturn(false);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(responsableEjemplo));
        when(sedeRepository.save(any(Sede.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SedeResponseDTO resultado = sedeService.actualizar(10, request);

        assertThat(resultado.getNombre()).isEqualTo("Sede Norte Modificada");
        assertThat(resultado.getDireccion()).isEqualTo("Nueva Direccion 123");
    }

    @Test
    @DisplayName("actualizar sede lanza DuplicateResourceException si el nombre ya pertenece a otra sede")
    void actualizar_NombreDuplicado_LanzaExcepcion() {
        SedeRequestDTO request = SedeRequestDTO.builder()
                .nombre("Sede Central")
                .direccion("Direccion")
                .build();

        when(sedeRepository.findById(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.existsByNombreIgnoreCaseAndIdSedeNot("Sede Central", 10)).thenReturn(true);

        assertThatThrownBy(() -> sedeService.actualizar(10, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe otra sede con el nombre 'Sede Central'");
    }

    @Test
    @DisplayName("eliminar sede sin dependencias se realiza con éxito")
    void eliminar_SinDependencias_Exitoso() {
        when(sedeRepository.findById(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(0L);
        when(sedeRepository.contarInventarioAsociado(10)).thenReturn(0L);
        when(sedeRepository.contarAlmacenesAsociados(10)).thenReturn(0L);

        sedeService.eliminar(10);

        verify(sedeRepository).delete(sedeEjemplo);
    }

    @Test
    @DisplayName("eliminar sede con usuarios asociados lanza ResourceInUseException")
    void eliminar_ConUsuarios_LanzaResourceInUseException() {
        when(sedeRepository.findById(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(2L);

        assertThatThrownBy(() -> sedeService.eliminar(10))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("tiene 2 usuario(s) asociado(s)");

        verify(sedeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("eliminar sede con inventario asociado lanza ResourceInUseException")
    void eliminar_ConInventario_LanzaResourceInUseException() {
        when(sedeRepository.findById(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(0L);
        when(sedeRepository.contarInventarioAsociado(10)).thenReturn(14L);

        assertThatThrownBy(() -> sedeService.eliminar(10))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("sus almacenes tienen inventario registrado");

        verify(sedeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("eliminar sede con almacenes asociados lanza ResourceInUseException")
    void eliminar_ConAlmacenes_LanzaResourceInUseException() {
        when(sedeRepository.findById(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.contarUsuariosAsociados(10)).thenReturn(0L);
        when(sedeRepository.contarInventarioAsociado(10)).thenReturn(0L);
        when(sedeRepository.contarAlmacenesAsociados(10)).thenReturn(1L);

        assertThatThrownBy(() -> sedeService.eliminar(10))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("tiene 1 almacén(es) asociado(s)");

        verify(sedeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("cambiarEstado actualiza el campo activo de la sede")
    void cambiarEstado_ActualizaActivo() {
        when(sedeRepository.findByIdWithResponsable(10)).thenReturn(Optional.of(sedeEjemplo));
        when(sedeRepository.save(any(Sede.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SedeResponseDTO resultado = sedeService.cambiarEstado(10, false);

        assertThat(resultado.getActivo()).isFalse();
        verify(sedeRepository).save(sedeEjemplo);
    }

    @Test
    @DisplayName("obtenerResponsablesDisponibles retorna usuarios activos no bloqueados")
    void obtenerResponsablesDisponibles_Exitoso() {
        Usuario inactivo = Usuario.builder()
                .idUsuario(2)
                .nombres("Pedro")
                .apellidos("Ruiz")
                .activo(false)
                .bloqueado(false)
                .build();

        Usuario bloqueado = Usuario.builder()
                .idUsuario(3)
                .nombres("Juan")
                .apellidos("Diaz")
                .activo(true)
                .bloqueado(true)
                .build();

        when(usuarioRepository.findAll()).thenReturn(List.of(responsableEjemplo, inactivo, bloqueado));

        List<ResponsableSedeResponseDTO> resultado = sedeService.obtenerResponsablesDisponibles();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1);
        assertThat(resultado.get(0).getNombreCompleto()).isEqualTo("Carlos García");
    }
}
