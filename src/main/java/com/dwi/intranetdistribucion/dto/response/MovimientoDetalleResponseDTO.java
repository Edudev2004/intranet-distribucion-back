package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoDetalleResponseDTO {

    private Integer idProducto;
    private String codigo;
    private String producto;
    private BigDecimal cantidad;
    private BigDecimal stockResultante;
    private Boolean alertaStock;
    private String mensajeAlerta;
}
