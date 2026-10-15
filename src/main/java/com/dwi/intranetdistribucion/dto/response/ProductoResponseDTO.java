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
public class ProductoResponseDTO {

    private Integer idProducto;
    private String codigo;
    private String nombre;
    private Integer idCategoria;
    private String categoria;
    private Integer idUnidad;
    private String unidadMedida;
    private BigDecimal stockMinimo;
    private BigDecimal pesoKg;
}
