package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "programacion_ruta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramacionRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programacion")
    private Integer idProgramacion;

    @Column(name = "id_ruta", nullable = false)
    private Integer idRuta;

    @Column(name = "id_vehiculo", nullable = false)
    private Integer idVehiculo;

    @Column(name = "id_conductor", nullable = false)
    private Integer idConductor;

    @Column(name = "inicio_programado", nullable = false)
    private OffsetDateTime inicioProgramado;

    @Column(name = "fin_programado", nullable = false)
    private OffsetDateTime finProgramado;

    @Column(name = "inicio_real")
    private OffsetDateTime inicioReal;

    @Column(name = "fin_real")
    private OffsetDateTime finReal;

    @Column(name = "id_estado_programacion", nullable = false)
    private Short idEstadoProgramacion;
}