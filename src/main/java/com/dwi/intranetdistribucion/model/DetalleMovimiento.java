package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_movimiento")
@IdClass(DetalleMovimientoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleMovimiento {

    @Id
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @Id
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "cantidad", columnDefinition = "numeric", nullable = false)
    private BigDecimal cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimiento", insertable = false, updatable = false)
    private MovimientoInventario movimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Producto producto;
}
