package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.AprobacionPedidoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AprobacionPedidoResponseDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.model.*;
import com.dwi.intranetdistribucion.repository.*;
import com.dwi.intranetdistribucion.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoAprobacionServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AprobacionPedidoRepository aprobacionPedidoRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Usuario supervisor;
    private Usuario cliente;
    private Pedido pedidoPendiente;

    @BeforeEach
    void setUp() {
        Rol rolSupervisor = Rol.builder()
                .idRol((short) 2)
                .nombre("SUPERVISOR")
                .descripcion("Supervisor")
                .build();

        supervisor = Usuario.builder()
                .idUsuario(1)
                .nombres("Carlos")
                .apellidos("Gomez")
                .correo("supervisor@distribucion.com")
                .rol(rolSupervisor)
                .activo(true)
                .build();

        Rol rolCliente = Rol.builder()
                .idRol((short) 4)
                .nombre("CLIENTE")
                .descripcion("Cliente")
                .build();

        cliente = Usuario.builder()
                .idUsuario(2)
                .nombres("Juan")
                .apellidos("Perez")
                .correo("cliente@correo.com")
                .rol(rolCliente)
                .activo(true)
                .build();

        pedidoPendiente = Pedido.builder()
                .idPedido(100L)
                .idCliente(10)
                .idAlmacen(1)
                .idEstadoPedido((short) 1) // 1 = Pendiente
                .fechaPedido(OffsetDateTime.now())
                .fechaEntregaSolicitada(LocalDate.now().plusDays(2))
                .observacion("Pedido de prueba")
                .build();
    }

    @Test
    @DisplayName("Debe aprobar un pedido pendiente correctamente por un supervisor")
    void debeAprobarPedidoCorrectamente() {
        AprobacionPedidoRequestDTO request = new AprobacionPedidoRequestDTO();
        request.setDecision("APROBADO");
        request.setMotivo("Cumple con límite de crédito y stock disponible");

        when(usuarioRepository.findByCorreoIgnoreCase("supervisor@distribucion.com"))
                .thenReturn(Optional.of(supervisor));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoPendiente));
        when(aprobacionPedidoRepository.existsByPedidoIdPedido(100L)).thenReturn(false);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        AprobacionPedido aprobacionMock = AprobacionPedido.builder()
                .idAprobacion(1L)
                .pedido(pedidoPendiente)
                .usuario(supervisor)
                .decision("APROBADO")
                .motivo(request.getMotivo())
                .fecha(OffsetDateTime.now())
                .build();

        when(aprobacionPedidoRepository.save(any(AprobacionPedido.class))).thenReturn(aprobacionMock);

        AprobacionPedidoResponseDTO response = pedidoService.aprobarRechazarPedido(100L, request, "supervisor@distribucion.com");

        assertNotNull(response);
        assertEquals("APROBADO", response.getDecision());
        assertEquals(100L, response.getIdPedido());
        assertEquals("Carlos Gomez", response.getUsuarioAprobador());
        assertEquals((short) 2, pedidoPendiente.getIdEstadoPedido()); // 2 = Aprobado
        verify(pedidoRepository).save(pedidoPendiente);
    }

    @Test
    @DisplayName("Debe rechazar un pedido pendiente y registrar el motivo obligatorio")
    void debeRechazarPedidoConMotivo() {
        AprobacionPedidoRequestDTO request = new AprobacionPedidoRequestDTO();
        request.setDecision("RECHAZADO");
        request.setMotivo("Cliente presenta deuda vencida pendiente");

        when(usuarioRepository.findByCorreoIgnoreCase("supervisor@distribucion.com"))
                .thenReturn(Optional.of(supervisor));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoPendiente));
        when(aprobacionPedidoRepository.existsByPedidoIdPedido(100L)).thenReturn(false);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        AprobacionPedido aprobacionMock = AprobacionPedido.builder()
                .idAprobacion(2L)
                .pedido(pedidoPendiente)
                .usuario(supervisor)
                .decision("RECHAZADO")
                .motivo(request.getMotivo())
                .fecha(OffsetDateTime.now())
                .build();

        when(aprobacionPedidoRepository.save(any(AprobacionPedido.class))).thenReturn(aprobacionMock);

        AprobacionPedidoResponseDTO response = pedidoService.aprobarRechazarPedido(100L, request, "supervisor@distribucion.com");

        assertNotNull(response);
        assertEquals("RECHAZADO", response.getDecision());
        assertEquals("Cliente presenta deuda vencida pendiente", response.getMotivo());
        assertEquals((short) 3, pedidoPendiente.getIdEstadoPedido()); // 3 = Rechazado
    }

    @Test
    @DisplayName("Debe lanzar excepción si se rechaza sin especificar motivo")
    void debeFallarAlRechazarSinMotivo() {
        AprobacionPedidoRequestDTO request = new AprobacionPedidoRequestDTO();
        request.setDecision("RECHAZADO");
        request.setMotivo(""); // Motivo vacío

        when(usuarioRepository.findByCorreoIgnoreCase("supervisor@distribucion.com"))
                .thenReturn(Optional.of(supervisor));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoPendiente));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                pedidoService.aprobarRechazarPedido(100L, request, "supervisor@distribucion.com"));

        assertTrue(ex.getMessage().contains("motivo"));
        verify(aprobacionPedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar si el usuario no tiene rol de supervisor o administrador")
    void debeFallarSiUsuarioNoEsSupervisor() {
        AprobacionPedidoRequestDTO request = new AprobacionPedidoRequestDTO();
        request.setDecision("APROBADO");

        when(usuarioRepository.findByCorreoIgnoreCase("cliente@correo.com"))
                .thenReturn(Optional.of(cliente));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                pedidoService.aprobarRechazarPedido(100L, request, "cliente@correo.com"));

        assertTrue(ex.getMessage().contains("Supervisor"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar si el pedido no está en estado pendiente")
    void debeFallarSiPedidoNoEstaPendiente() {
        pedidoPendiente.setIdEstadoPedido((short) 2); // Ya aprobado

        AprobacionPedidoRequestDTO request = new AprobacionPedidoRequestDTO();
        request.setDecision("APROBADO");

        when(usuarioRepository.findByCorreoIgnoreCase("supervisor@distribucion.com"))
                .thenReturn(Optional.of(supervisor));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoPendiente));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                pedidoService.aprobarRechazarPedido(100L, request, "supervisor@distribucion.com"));

        assertTrue(ex.getMessage().contains("Pendiente"));
    }
}
