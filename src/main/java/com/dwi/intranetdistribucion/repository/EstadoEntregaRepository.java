package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.EstadoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoEntregaRepository extends JpaRepository<EstadoEntrega, Short> {
    Optional<EstadoEntrega> findByNombreIgnoreCase(String nombre);
}
