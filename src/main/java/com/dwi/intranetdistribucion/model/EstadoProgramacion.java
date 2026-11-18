package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estado_programacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoProgramacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_programacion")
    private Short idEstadoProgramacion;

    @Column(nullable = false, unique = true)
    private String nombre;
}