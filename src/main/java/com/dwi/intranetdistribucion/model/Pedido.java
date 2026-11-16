package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

// Cabecera de pedido (public.pedido). Cliente, almacén, usuario y estado se manejan como ids.
@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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