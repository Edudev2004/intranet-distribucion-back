package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comprobante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Long idComprobante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_comprobante", nullable = false)
    private TipoComprobante tipoComprobante;

    @Column(name = "serie", columnDefinition = "varchar", nullable = false)
    private String serie;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private OffsetDateTime fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        if (this.fechaEmision == null) {
            this.fechaEmision = OffsetDateTime.now();
        }
    }

    public String getNumeroCompleto() {
        return String.format("%s-%08d", serie, numero);
    }
}
