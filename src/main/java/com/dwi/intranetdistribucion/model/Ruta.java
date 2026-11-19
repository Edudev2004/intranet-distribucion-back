package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ruta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zona", nullable = false)
    private Zona zona;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
