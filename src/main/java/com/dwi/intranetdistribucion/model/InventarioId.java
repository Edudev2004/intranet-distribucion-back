package com.dwi.intranetdistribucion.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InventarioId implements Serializable {

    private Integer idAlmacen;
    private Integer idProducto;
}
