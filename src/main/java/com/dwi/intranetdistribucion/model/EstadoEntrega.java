package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estado_entrega")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoEntrega {

    @Id
    @Column(name = "id_estado_entrega")
    private Short idEstadoEntrega;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;
}