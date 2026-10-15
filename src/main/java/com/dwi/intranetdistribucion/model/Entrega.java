package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

// Vista de solo lectura de public.entrega: este módulo solo la consulta y la vincula.
// Pedido y programación se exponen como ids hasta que existan sus entidades.
@Entity
@Table(name = "entrega")
@Immutable
@Getter
@NoArgsConstructor
public class Entrega {

    @Id
    @Column(name = "id_entrega")
    private Long idEntrega;

    @Column(name = "id_programacion", nullable = false)
    private Integer idProgramacion;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "id_estado_entrega", nullable = false)
    private Short idEstadoEntrega;

    @Column(name = "orden_visita")
    private Short ordenVisita;

    @Column(name = "hora_estimada")
    private OffsetDateTime horaEstimada;

    @Column(name = "hora_entrega_real")
    private OffsetDateTime horaEntregaReal;
}
