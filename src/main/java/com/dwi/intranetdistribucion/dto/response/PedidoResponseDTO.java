package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {

    private Long idPedido;
    private Integer idCliente;
    private String cliente;
    private Integer idAlmacen;
    private String almacen;
    private Integer idEstadoPedido;
    private String estado;
    private OffsetDateTime fechaPedido;
    private LocalDate fechaEntregaSolicitada;
    private String observacion;
    private BigDecimal total;
    private List<DetallePedidoResponseDTO> detalles;
}