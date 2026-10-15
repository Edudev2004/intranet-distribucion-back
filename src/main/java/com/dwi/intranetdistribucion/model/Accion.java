package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_accion")
    private Short idAccion;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false, unique = true)
    private String nombre;
}
