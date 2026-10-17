package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.AjusteStockRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoItemRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.*;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.GlobalExceptionHandler;
import com.dwi.intranetdistribucion.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/inventario retorna consulta con status 200 y filtros")
    void consultarInventario_Status200() throws Exception {
        InventarioItemDTO item = InventarioItemDTO.builder()
                .idAlmacen(10)
                .almacen("Almacén Central")
                .idProducto(100)
                .codigo("P-100")
                .producto("Arroz")
                .stockActual(new BigDecimal("50.00"))
                .stockMinimo(new BigDecimal("10.00"))
                .bajoStock(false)
                .nivelAlerta("NORMAL")
                .build();

        InventarioConsultaDTO consulta = InventarioConsultaDTO.builder()
                .items(List.of(item))
                .totalProductos(1)
                .totalBajoStock(0)
                .totalSinStock(0)
                .build();

        when(inventarioService.consultarInventario(10, null, false, null)).thenReturn(consulta);

        mockMvc.perform(get("/api/v1/inventario")
                        .param("idAlmacen", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos", is(1)))
                .andExpect(jsonPath("$.items[0].producto", is("Arroz")));
    }

    @Test
    @DisplayName("GET /api/v1/inventario/alertas retorna lista de alertas con status 200")
    void obtenerAlertas_Status200() throws Exception {
        AlertaStockDTO alerta = AlertaStockDTO.builder()
                .idAlmacen(10)
                .almacen("Almacén Central")
                .idProducto(101)
                .producto("Aceite")
                .stockActual(new BigDecimal("2.00"))
                .stockMinimo(new BigDecimal("10.00"))
                .nivelAlerta("BAJO_STOCK")
                .mensaje("Alerta de stock mínimo")
                .build();

        when(inventarioService.obtenerAlertas(10, null)).thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/v1/inventario/alertas")
                        .param("idAlmacen", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nivelAlerta", is("BAJO_STOCK")));
    }

    @Test
    @DisplayName("GET /api/v1/inventario/{idAlmacen}/{idProducto} retorna detalle con status 200")
    void obtenerStockProducto_Status200() throws Exception {
        InventarioItemDTO item = InventarioItemDTO.builder()
                .idAlmacen(10)
                .almacen("Almacén Central")
                .idProducto(100)
                .producto("Arroz")
                .stockActual(new BigDecimal("15.00"))
                .stockMinimo(new BigDecimal("10.00"))
                .bajoStock(false)
                .build();

        when(inventarioService.obtenerStockProducto(10, 100)).thenReturn(item);

        mockMvc.perform(get("/api/v1/inventario/10/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto", is(100)))
                .andExpect(jsonPath("$.producto", is("Arroz")));
    }

    @Test
    @DisplayName("POST /api/v1/inventario/movimientos con datos válidos retorna status 201")
    void registrarMovimiento_Valido_Status201() throws Exception {
        String jsonRequest = """
                {
                    "idAlmacen": 10,
                    "idTipoMovimiento": 1,
                    "observacion": "Ingreso compra",
                    "items": [
                        {
                            "idProducto": 100,
                            "cantidad": 20.0
                        }
                    ]
                }
                """;

        MovimientoResponseDTO responseDTO = MovimientoResponseDTO.builder()
                .idMovimiento(501L)
                .idAlmacen(10)
                .tipoMovimiento("ENTRADA_COMPRA")
                .naturaleza('E')
                .contieneAlertas(false)
                .build();

        when(inventarioService.registrarMovimiento(any(MovimientoRequestDTO.class), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/inventario/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimiento", is(501)))
                .andExpect(jsonPath("$.tipoMovimiento", is("ENTRADA_COMPRA")));
    }

    @Test
    @DisplayName("POST /api/v1/inventario/movimientos con stock insuficiente retorna status 400")
    void registrarMovimiento_StockInsuficiente_Status400() throws Exception {
        String jsonRequest = """
                {
                    "idAlmacen": 10,
                    "idTipoMovimiento": 2,
                    "items": [
                        {
                            "idProducto": 100,
                            "cantidad": 999.0
                        }
                    ]
                }
                """;

        when(inventarioService.registrarMovimiento(any(MovimientoRequestDTO.class), any()))
                .thenThrow(new BadRequestException("Stock insuficiente en el almacén"));

        mockMvc.perform(post("/api/v1/inventario/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/inventario/ajuste con datos válidos retorna status 200")
    void ajustarStock_Valido_Status200() throws Exception {
        String jsonRequest = """
                {
                    "idAlmacen": 10,
                    "idProducto": 100,
                    "nuevoStock": 35.0,
                    "motivo": "Ajuste inventario físico"
                }
                """;

        MovimientoResponseDTO responseDTO = MovimientoResponseDTO.builder()
                .idMovimiento(502L)
                .idAlmacen(10)
                .tipoMovimiento("ENTRADA_AJUSTE")
                .build();

        when(inventarioService.ajustarStock(any(AjusteStockRequestDTO.class), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/inventario/ajuste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimiento", is(502)));
    }

    @Test
    @DisplayName("GET /api/v1/inventario/tipos-movimiento retorna status 200")
    void obtenerTiposMovimiento_Status200() throws Exception {
        TipoMovimientoResponseDTO tipo = TipoMovimientoResponseDTO.builder()
                .idTipoMovimiento((short) 1)
                .nombre("ENTRADA_COMPRA")
                .naturaleza('E')
                .build();

        when(inventarioService.obtenerTiposMovimiento()).thenReturn(List.of(tipo));

        mockMvc.perform(get("/api/v1/inventario/tipos-movimiento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("ENTRADA_COMPRA")));
    }
}
