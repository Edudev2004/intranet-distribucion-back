package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.TipoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoIncidenciaRepository extends JpaRepository<TipoIncidencia, Short> {
}
