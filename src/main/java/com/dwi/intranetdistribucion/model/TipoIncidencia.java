package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_incidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_incidencia")
    private Short idTipoIncidencia;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false, unique = true)
    private String nombre;
}
