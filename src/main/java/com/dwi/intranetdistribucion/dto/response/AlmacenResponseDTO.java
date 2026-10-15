package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlmacenResponseDTO {

    private Integer idAlmacen;
    private Integer idSede;
    private String sede;
    private String nombre;
    private Short idTipoAlmacen;
    private String tipoAlmacen;
    private BigDecimal capacidadMaxima;
    private String ubicacion;
    private Boolean activo;
}