package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Short> {

    default Optional<UnidadMedida> findById(Integer id) {
        return id != null ? findById(id.shortValue()) : Optional.empty();
    }
}
