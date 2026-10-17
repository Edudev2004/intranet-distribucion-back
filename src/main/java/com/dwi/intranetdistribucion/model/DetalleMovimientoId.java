package com.dwi.intranetdistribucion.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DetalleMovimientoId implements Serializable {

    private Long idMovimiento;
    private Integer idProducto;
}
