package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.TipoMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMantenimientoRepository extends JpaRepository<TipoMantenimiento, Short> {
}