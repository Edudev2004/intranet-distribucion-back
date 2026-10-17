package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "inventario")
@IdClass(InventarioId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @Column(name = "id_almacen")
    private Integer idAlmacen;

    @Id
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "stock_actual", columnDefinition = "numeric", nullable = false)
    private BigDecimal stockActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen", insertable = false, updatable = false)
    private Almacen almacen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Producto producto;

    public void aumentarStock(BigDecimal cantidad) {
        if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal actual = (this.stockActual != null) ? this.stockActual : BigDecimal.ZERO;
            this.stockActual = actual.add(cantidad);
        }
    }

    public void disminuirStock(BigDecimal cantidad) {
        if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal actual = (this.stockActual != null) ? this.stockActual : BigDecimal.ZERO;
            this.stockActual = actual.subtract(cantidad);
        }
    }
}
