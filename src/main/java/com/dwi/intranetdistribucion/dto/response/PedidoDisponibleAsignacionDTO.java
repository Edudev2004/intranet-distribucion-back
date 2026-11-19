package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDisponibleAsignacionDTO {

    private Long idPedido;
    private Integer idCliente;
    private String cliente;
    private String numeroDocumentoCliente;
    private String direccionEntrega;
    private String zonaCliente;
    private Integer idAlmacen;
    private String almacen;
    private Short idEstadoPedido;
    private String estado;
    private OffsetDateTime fechaPedido;
    private LocalDate fechaEntregaSolicitada;
    private BigDecimal pesoTotalKg;
    private BigDecimal importeTotal;
}
