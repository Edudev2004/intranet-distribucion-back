package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.OffsetDateTime;

// Vista de solo lectura de public.pedido: la gestión de pedidos pertenece a otro módulo.
// Cliente, almacén, usuario y estado se exponen como ids hasta que existan sus entidades.
@Entity
@Table(name = "pedido")
@Immutable
@Getter
@NoArgsConstructor
public class Pedido {

    @Id
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_almacen", nullable = false)
    private Integer idAlmacen;

    @Column(name = "id_usuario_registra", nullable = false)
    private Integer idUsuarioRegistra;

    @Column(name = "id_estado_pedido", nullable = false)
    private Short idEstadoPedido;

    @Column(name = "fecha_pedido", nullable = false)
    private OffsetDateTime fechaPedido;

    @Column(name = "fecha_entrega_solicitada")
    private LocalDate fechaEntregaSolicitada;

    @Column(name = "observacion", columnDefinition = "varchar")
    private String observacion;
}
