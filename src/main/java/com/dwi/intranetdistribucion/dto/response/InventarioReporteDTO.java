package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioReporteDTO {

    private Integer idAlmacen;
    private String almacen;
    private String sede;
    private Integer idProducto;
    private String codigo;
    private String producto;
    private String categoria;
    private String unidad;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private Boolean bajoStock;
}
