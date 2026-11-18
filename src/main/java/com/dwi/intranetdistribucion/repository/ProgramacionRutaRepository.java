package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.ProgramacionRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramacionRutaRepository extends JpaRepository<ProgramacionRuta, Integer> {

    List<ProgramacionRuta> findByIdEstadoProgramacion(Short idEstadoProgramacion);

    @Query("SELECT ep.nombre FROM EstadoProgramacion ep WHERE ep.idEstadoProgramacion = :idEstado")
    Optional<String> obtenerNombreEstadoProgramacion(@Param("idEstado") Integer idEstado);
}