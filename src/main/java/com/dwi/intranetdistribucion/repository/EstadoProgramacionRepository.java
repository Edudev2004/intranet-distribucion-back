package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.EstadoProgramacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoProgramacionRepository extends JpaRepository<EstadoProgramacion, Short> {
}