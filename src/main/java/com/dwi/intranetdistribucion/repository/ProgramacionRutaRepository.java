package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.ProgramacionRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramacionRutaRepository extends JpaRepository<ProgramacionRuta, Integer> {

    @Query(value = "SELECT nombre FROM estado_programacion WHERE id_estado_programacion = :idEstado", nativeQuery = true)
    Optional<String> obtenerNombreEstadoProgramacion(@Param("idEstado") Integer idEstado);
}