package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoAlmacenResponseDTO {

    private Integer idProducto;
    private String codigo;
    private String producto;
    private String categoria;
    private BigDecimal stockActual;
}