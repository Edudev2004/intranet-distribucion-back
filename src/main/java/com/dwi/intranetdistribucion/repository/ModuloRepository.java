package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Short> {
    Optional<Modulo> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
