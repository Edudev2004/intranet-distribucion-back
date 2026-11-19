package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportePedidoItemDTO {

    private Long idPedido;
    private String cliente;
    private String almacen;
    private Integer idEstadoPedido;
    private String estado;
    private String fechaPedido;
    private BigDecimal total;
}
