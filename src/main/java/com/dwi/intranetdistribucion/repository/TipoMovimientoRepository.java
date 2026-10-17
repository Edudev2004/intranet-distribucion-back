package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimiento, Short> {

    Optional<TipoMovimiento> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<TipoMovimiento> findByNaturaleza(Character naturaleza);
}
