package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResumenDTO {

    private Long idPedido;
    private String cliente;
    private String almacen;
    private Integer idEstadoPedido;
    private String estado;
    private String fechaPedido;
    private BigDecimal total;
}