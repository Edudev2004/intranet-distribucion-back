package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "conductor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conductor")
    private Integer idConductor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    @Column(name = "nro_licencia", nullable = false, unique = true)
    private String nroLicencia;

    @Column(name = "categoria_licencia", nullable = false)
    private String categoriaLicencia;

    @Column(name = "fecha_venc_licencia", nullable = false)
    private LocalDate fechaVencLicencia;

    @Column(name = "telefono")
    private String telefono;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
