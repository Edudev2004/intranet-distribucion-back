package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sede")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sede")
    private Integer idSede;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false, unique = true)
    private String nombre;

    @Column(name = "direccion", columnDefinition = "varchar", nullable = false)
    private String direccion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
