package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
}
