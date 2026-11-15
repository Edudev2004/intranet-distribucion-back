package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(name = "placa", columnDefinition = "varchar", nullable = false, unique = true)
    private String placa;

    @Column(name = "marca", columnDefinition = "varchar")
    private String marca;

    @Column(name = "modelo", columnDefinition = "varchar")
    private String modelo;

    @Column(name = "capacidad_kg", columnDefinition = "numeric", nullable = false)
    private BigDecimal capacidadKg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_vehiculo", nullable = false)
    private EstadoVehiculo estado;
}