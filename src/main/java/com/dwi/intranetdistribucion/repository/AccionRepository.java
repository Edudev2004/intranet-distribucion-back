package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Accion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccionRepository extends JpaRepository<Accion, Short> {
    Optional<Accion> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
