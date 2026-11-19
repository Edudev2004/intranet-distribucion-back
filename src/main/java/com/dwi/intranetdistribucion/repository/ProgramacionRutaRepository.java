package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.ProgramacionRuta;
import com.dwi.intranetdistribucion.repository.projection.ProgramacionReporteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramacionRutaRepository extends JpaRepository<ProgramacionRuta, Integer> {

    @Query(value = "SELECT nombre FROM estado_programacion WHERE id_estado_programacion = :idEstado", nativeQuery = true)
    Optional<String> obtenerNombreEstadoProgramacion(@Param("idEstado") Integer idEstado);

    @Query(value = """
            SELECT pr.id_programacion AS "idProgramacion",
                   pr.id_ruta AS "idRuta",
                   r.nombre AS "ruta",
                   pr.id_vehiculo AS "idVehiculo",
                   v.placa AS "vehiculoPlaca",
                   v.marca AS "vehiculoMarca",
                   v.modelo AS "vehiculoModelo",
                   v.capacidad_kg AS "vehiculoCapacidadKg",
                   pr.id_conductor AS "idConductor",
                   c.nombres AS "conductorNombres",
                   c.apellidos AS "conductorApellidos",
                   c.dni AS "conductorDni",
                   c.nro_licencia AS "conductorLicencia",
                   pr.inicio_programado AS "inicioProgramado",
                   pr.fin_programado AS "finProgramado",
                   pr.inicio_real AS "inicioReal",
                   pr.fin_real AS "finReal",
                   pr.id_estado_programacion AS "idEstadoProgramacion",
                   ep.nombre AS "estadoProgramacion"
            FROM programacion_ruta pr
            JOIN ruta r ON r.id_ruta = pr.id_ruta
            JOIN vehiculo v ON v.id_vehiculo = pr.id_vehiculo
            JOIN conductor c ON c.id_conductor = pr.id_conductor
            JOIN estado_programacion ep ON ep.id_estado_programacion = pr.id_estado_programacion
            WHERE (CAST(:fechaInicio AS date) IS NULL OR CAST(pr.inicio_programado AT TIME ZONE 'America/Lima' AS date) >= :fechaInicio)
              AND (CAST(:fechaFin AS date) IS NULL OR CAST(pr.inicio_programado AT TIME ZONE 'America/Lima' AS date) <= :fechaFin)
              AND (CAST(:idVehiculo AS integer) IS NULL OR pr.id_vehiculo = :idVehiculo)
              AND (CAST(:idConductor AS integer) IS NULL OR pr.id_conductor = :idConductor)
            ORDER BY pr.id_programacion DESC
            """, nativeQuery = true)
    List<ProgramacionReporteView> obtenerReporteProductividad(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idVehiculo") Integer idVehiculo,
            @Param("idConductor") Integer idConductor);
}