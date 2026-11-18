package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder; // <-- 1. Importa Builder
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "aprobacion_pedido")
@Data
@Builder // <-- 2. Añade la anotación aquí
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aprobacion")
    private Long idAprobacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "decision", nullable = false)
    private String decision; // 'APROBADO' o 'RECHAZADO'

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha = OffsetDateTime.now();
}