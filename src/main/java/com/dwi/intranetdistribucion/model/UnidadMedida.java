package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidad_medida")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private Short idUnidad;

    @Column(name = "codigo", columnDefinition = "varchar", nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false)
    private String nombre;

    public Integer getIdUnidad() {
        return idUnidad != null ? idUnidad.intValue() : null;
    }

    public void setIdUnidad(Integer idUnidad) {
        this.idUnidad = idUnidad != null ? idUnidad.shortValue() : null;
    }

    public Short getIdUnidadShort() {
        return idUnidad;
    }
}
