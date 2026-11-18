package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDisponibleDTO {

    private Integer idProducto;
    private String codigo;
    private String producto;
    private BigDecimal precioUnitario;
    private BigDecimal stockActual;
}