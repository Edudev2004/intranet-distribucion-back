package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.AjusteStockRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoItemRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.*;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.*;
import com.dwi.intranetdistribucion.repository.*;
import com.dwi.intranetdistribucion.repository.projection.InventarioDetalleView;
import com.dwi.intranetdistribucion.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TipoMovimientoRepository tipoMovimientoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Almacen almacenEjemplo;
    private Producto productoEjemplo;
    private Usuario usuarioEjemplo;
    private TipoMovimiento tipoEntrada;
    private TipoMovimiento tipoSalida;

    @BeforeEach
    void setUp() {
        Sede sede = Sede.builder().idSede(1).nombre("Sede Central").build();

        almacenEjemplo = Almacen.builder()
                .idAlmacen(10)
                .nombre("Almacén Principal")
                .sede(sede)
                .build();

        productoEjemplo = Producto.builder()
                .idProducto(100)
                .codigo("PROD-01")
                .nombre("Aceite Vegetal 1L")
                .stockMinimo(new BigDecimal("10.00"))
                .precioUnitario(new BigDecimal("8.50"))
                .categoria(Categoria.builder().idCategoria(1).nombre("Abarrotes").build())
                .unidadMedida(UnidadMedida.builder().idUnidad((short) 1).codigo("UND").nombre("Unidad").build())
                .build();

        usuarioEjemplo = Usuario.builder()
                .idUsuario(1)
                .username("almacenero")
                .nombres("Juan")
                .apellidos("Pérez")
                .correo("almacenero@distribucion.com")
                .build();

        tipoEntrada = TipoMovimiento.builder()
                .idTipoMovimiento((short) 1)
                .nombre("ENTRADA_COMPRA")
                .naturaleza('E')
                .build();

        tipoSalida = TipoMovimiento.builder()
                .idTipoMovimiento((short) 2)
                .nombre("SALIDA_VENTA")
                .naturaleza('S')
                .build();
    }

    @Test
    @DisplayName("consultarInventario filtra por almacén y categoría y calcula alertas de stock mínimo")
    void consultarInventario_ConFiltros_CalculaAlertas() {
        InventarioDetalleView v1 = mock(InventarioDetalleView.class);
        when(v1.getIdAlmacen()).thenReturn(10);
        when(v1.getAlmacen()).thenReturn("Almacén Principal");
        when(v1.getProducto()).thenReturn("Aceite Vegetal 1L");
        when(v1.getStockActual()).thenReturn(new BigDecimal("5.00")); // Bajo stock (< 10)
        when(v1.getStockMinimo()).thenReturn(new BigDecimal("10.00"));

        InventarioDetalleView v2 = mock(InventarioDetalleView.class);
        when(v2.getIdAlmacen()).thenReturn(10);
        when(v2.getAlmacen()).thenReturn("Almacén Principal");
        when(v2.getProducto()).thenReturn("Arroz 1kg");
        when(v2.getStockActual()).thenReturn(new BigDecimal("25.00")); // Normal (> 10)
        when(v2.getStockMinimo()).thenReturn(new BigDecimal("10.00"));

        when(inventarioRepository.consultarInventario(eq(10), eq(1), eq(false), isNull()))
                .thenReturn(List.of(v1, v2));
        when(inventarioRepository.obtenerNombreAlmacen(10))
                .thenReturn(Optional.of("Almacén Principal"));

        InventarioConsultaDTO resultado = inventarioService.consultarInventario(10, 1, false, null);

        assertThat(resultado.getTotalProductos()).isEqualTo(2);
        assertThat(resultado.getTotalBajoStock()).isEqualTo(1);
        assertThat(resultado.getTotalSinStock()).isEqualTo(0);
        assertThat(resultado.getItems().get(0).getBajoStock()).isTrue();
        assertThat(resultado.getItems().get(0).getNivelAlerta()).isEqualTo("BAJO_STOCK");
        assertThat(resultado.getItems().get(1).getBajoStock()).isFalse();
        assertThat(resultado.getItems().get(1).getNivelAlerta()).isEqualTo("NORMAL");
    }

    @Test
    @DisplayName("obtenerAlertas retorna productos con stock actual menor o igual al mínimo")
    void obtenerAlertas_RetornaAlertas() {
        InventarioDetalleView v = mock(InventarioDetalleView.class);
        when(v.getIdAlmacen()).thenReturn(10);
        when(v.getAlmacen()).thenReturn("Almacén Principal");
        when(v.getSede()).thenReturn("Sede Central");
        when(v.getIdProducto()).thenReturn(100);
        when(v.getCodigo()).thenReturn("PROD-01");
        when(v.getProducto()).thenReturn("Aceite Vegetal 1L");
        when(v.getUnidad()).thenReturn("UND");
        when(v.getStockActual()).thenReturn(new BigDecimal("4.00"));
        when(v.getStockMinimo()).thenReturn(new BigDecimal("10.00"));

        when(inventarioRepository.consultarAlertas(10, null)).thenReturn(List.of(v));

        List<AlertaStockDTO> alertas = inventarioService.obtenerAlertas(10, null);

        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getNivelAlerta()).isEqualTo("BAJO_STOCK");
        assertThat(alertas.get(0).getDiferencia()).isEqualByComparingTo(new BigDecimal("-6.00"));
        assertThat(alertas.get(0).getMensaje()).contains("inferior al mínimo");
    }

    @Test
    @DisplayName("registrarMovimiento con entrada actualiza e incrementa automáticamente el stock")
    void registrarMovimiento_Entrada_IncrementaStock() {
        MovimientoRequestDTO request = MovimientoRequestDTO.builder()
                .idAlmacen(10)
                .idTipoMovimiento((short) 1)
                .observacion("Ingreso de mercadería por compra")
                .items(List.of(MovimientoItemRequestDTO.builder()
                        .idProducto(100)
                        .cantidad(new BigDecimal("15.00"))
                        .build()))
                .build();

        Inventario inventarioExistente = Inventario.builder()
                .idAlmacen(10)
                .idProducto(100)
                .stockActual(new BigDecimal("5.00"))
                .build();

        when(almacenRepository.findById(10)).thenReturn(Optional.of(almacenEjemplo));
        when(tipoMovimientoRepository.findById((short) 1)).thenReturn(Optional.of(tipoEntrada));
        when(usuarioRepository.findByUsernameIgnoreCase("almacenero")).thenReturn(Optional.of(usuarioEjemplo));
        when(productoRepository.findById(100)).thenReturn(Optional.of(productoEjemplo));
        when(inventarioRepository.findByIdAlmacenAndIdProducto(10, 100))
                .thenReturn(Optional.of(inventarioExistente));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenAnswer(i -> {
            MovimientoInventario m = i.getArgument(0);
            m.setIdMovimiento(1001L);
            return m;
        });
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        MovimientoResponseDTO response = inventarioService.registrarMovimiento(request, "almacenero");

        assertThat(response.getIdMovimiento()).isEqualTo(1001L);
        assertThat(response.getNaturaleza()).isEqualTo('E');
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getStockResultante()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.isContieneAlertas()).isFalse();

        verify(inventarioRepository).save(argThat(inv ->
                inv.getStockActual().compareTo(new BigDecimal("20.00")) == 0
        ));
    }

    @Test
    @DisplayName("registrarMovimiento con salida actualiza y decrementa el stock")
    void registrarMovimiento_Salida_DecrementaStock() {
        MovimientoRequestDTO request = MovimientoRequestDTO.builder()
                .idAlmacen(10)
                .idTipoMovimiento((short) 2)
                .observacion("Salida por venta a cliente")
                .items(List.of(MovimientoItemRequestDTO.builder()
                        .idProducto(100)
                        .cantidad(new BigDecimal("8.00"))
                        .build()))
                .build();

        Inventario inventarioExistente = Inventario.builder()
                .idAlmacen(10)
                .idProducto(100)
                .stockActual(new BigDecimal("15.00"))
                .build();

        when(almacenRepository.findById(10)).thenReturn(Optional.of(almacenEjemplo));
        when(tipoMovimientoRepository.findById((short) 2)).thenReturn(Optional.of(tipoSalida));
        when(usuarioRepository.findByUsernameIgnoreCase("almacenero")).thenReturn(Optional.of(usuarioEjemplo));
        when(productoRepository.findById(100)).thenReturn(Optional.of(productoEjemplo));
        when(inventarioRepository.findByIdAlmacenAndIdProducto(10, 100))
                .thenReturn(Optional.of(inventarioExistente));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenAnswer(i -> {
            MovimientoInventario m = i.getArgument(0);
            m.setIdMovimiento(1002L);
            return m;
        });
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        MovimientoResponseDTO response = inventarioService.registrarMovimiento(request, "almacenero");

        assertThat(response.getItems().get(0).getStockResultante()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(response.isContieneAlertas()).isTrue(); // 7.00 <= stockMinimo (10.00)
        assertThat(response.getItems().get(0).getAlertaStock()).isTrue();
    }

    @Test
    @DisplayName("registrarMovimiento con salida lanza BadRequestException si el stock es insuficiente")
    void registrarMovimiento_Salida_StockInsuficiente_LanzaExcepcion() {
        MovimientoRequestDTO request = MovimientoRequestDTO.builder()
                .idAlmacen(10)
                .idTipoMovimiento((short) 2)
                .items(List.of(MovimientoItemRequestDTO.builder()
                        .idProducto(100)
                        .cantidad(new BigDecimal("50.00"))
                        .build()))
                .build();

        Inventario inventarioExistente = Inventario.builder()
                .idAlmacen(10)
                .idProducto(100)
                .stockActual(new BigDecimal("10.00"))
                .build();

        when(almacenRepository.findById(10)).thenReturn(Optional.of(almacenEjemplo));
        when(tipoMovimientoRepository.findById((short) 2)).thenReturn(Optional.of(tipoSalida));
        when(usuarioRepository.findByUsernameIgnoreCase("almacenero")).thenReturn(Optional.of(usuarioEjemplo));
        when(productoRepository.findById(100)).thenReturn(Optional.of(productoEjemplo));
        when(inventarioRepository.findByIdAlmacenAndIdProducto(10, 100))
                .thenReturn(Optional.of(inventarioExistente));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> inventarioService.registrarMovimiento(request, "almacenero"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(inventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("ajustarStock con valor mayor genera movimiento de entrada por la diferencia")
    void ajustarStock_Incremento_GeneraEntrada() {
        AjusteStockRequestDTO request = AjusteStockRequestDTO.builder()
                .idAlmacen(10)
                .idProducto(100)
                .nuevoStock(new BigDecimal("25.00"))
                .motivo("Conteo físico inventario")
                .build();

        Inventario inventarioExistente = Inventario.builder()
                .idAlmacen(10)
                .idProducto(100)
                .stockActual(new BigDecimal("20.00"))
                .build();

        when(almacenRepository.findById(10)).thenReturn(Optional.of(almacenEjemplo));
        when(productoRepository.findById(100)).thenReturn(Optional.of(productoEjemplo));
        when(inventarioRepository.findByIdAlmacenAndIdProducto(10, 100))
                .thenReturn(Optional.of(inventarioExistente));
        when(tipoMovimientoRepository.findByNombreIgnoreCase("ENTRADA_AJUSTE"))
                .thenReturn(Optional.of(tipoEntrada));
        when(tipoMovimientoRepository.findById((short) 1)).thenReturn(Optional.of(tipoEntrada));
        when(usuarioRepository.findByUsernameIgnoreCase("almacenero")).thenReturn(Optional.of(usuarioEjemplo));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenAnswer(i -> {
            MovimientoInventario m = i.getArgument(0);
            m.setIdMovimiento(1003L);
            return m;
        });
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        MovimientoResponseDTO response = inventarioService.ajustarStock(request, "almacenero");

        assertThat(response.getItems().get(0).getCantidad()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(response.getItems().get(0).getStockResultante()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("ajustarStock con mismo valor no genera movimiento y confirma stock")
    void ajustarStock_MismoValor_NoGeneraMovimiento() {
        AjusteStockRequestDTO request = AjusteStockRequestDTO.builder()
                .idAlmacen(10)
                .idProducto(100)
                .nuevoStock(new BigDecimal("10.00"))
                .build();

        Inventario inventarioExistente = Inventario.builder()
                .idAlmacen(10)
                .idProducto(100)
                .stockActual(new BigDecimal("10.00"))
                .build();

        when(almacenRepository.findById(10)).thenReturn(Optional.of(almacenEjemplo));
        when(productoRepository.findById(100)).thenReturn(Optional.of(productoEjemplo));
        when(inventarioRepository.findByIdAlmacenAndIdProducto(10, 100))
                .thenReturn(Optional.of(inventarioExistente));

        MovimientoResponseDTO response = inventarioService.ajustarStock(request, "almacenero");

        assertThat(response.getIdMovimiento()).isNull();
        assertThat(response.getItems().get(0).getCantidad()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(movimientoInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerTiposMovimiento retorna lista de tipos configurados")
    void obtenerTiposMovimiento_RetornaLista() {
        when(tipoMovimientoRepository.findAll()).thenReturn(List.of(tipoEntrada, tipoSalida));

        List<TipoMovimientoResponseDTO> tipos = inventarioService.obtenerTiposMovimiento();

        assertThat(tipos).hasSize(2);
        assertThat(tipos.get(0).getNombre()).isEqualTo("ENTRADA_COMPRA");
        assertThat(tipos.get(1).getNombre()).isEqualTo("SALIDA_VENTA");
    }
}
