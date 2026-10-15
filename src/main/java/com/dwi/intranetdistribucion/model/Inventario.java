package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

// Vista de solo lectura de public.inventario: el stock lo actualizan los movimientos de inventario.
@Entity
@Table(name = "inventario")
@IdClass(InventarioId.class)
@Immutable
@Getter
@NoArgsConstructor
public class Inventario {

    @Id
    @Column(name = "id_almacen")
    private Integer idAlmacen;

    @Id
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "stock_actual", columnDefinition = "numeric", nullable = false)
    private BigDecimal stockActual;
}
