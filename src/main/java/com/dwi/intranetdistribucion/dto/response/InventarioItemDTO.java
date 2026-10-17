package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioItemDTO {

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
    private BigDecimal precioUnitario;
    private Boolean bajoStock;
    private String nivelAlerta; // NORMAL, BAJO_STOCK, SIN_STOCK
    private String mensajeAlerta;
}
