package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByIdProgramacion(Integer idProgramacion);

    @Query("SELECT e.nombre FROM EstadoEntrega e WHERE e.idEstadoEntrega = :idEstado")
    Optional<String> obtenerNombreEstadoEntrega(@Param("idEstado") Integer idEstado);
}