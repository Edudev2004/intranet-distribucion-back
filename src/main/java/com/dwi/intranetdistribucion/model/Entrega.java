package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "entrega")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "foto_entrega_url")
    private String fotoEntregaUrl;

    @Column(name = "foto_guia_url")
    private String fotoGuiaUrl;

    @Column(name = "notas")
    private String notas;
}