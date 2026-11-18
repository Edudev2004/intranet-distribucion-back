package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
@IdClass(DetallePedidoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Id
    @Column(name = "id_pedido")
    private Long idPedido;

    @Id
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "cantidad", columnDefinition = "numeric", nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", columnDefinition = "numeric", nullable = false)
    private BigDecimal precioUnitario;
}