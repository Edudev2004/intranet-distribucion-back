package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.TipoComprobante;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoComprobanteRepository extends JpaRepository<TipoComprobante, Short> {

    // Bloquea la fila del tipo para serializar la numeración correlativa de sus comprobantes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TipoComprobante t WHERE t.idTipoComprobante = :id")
    Optional<TipoComprobante> findByIdParaEmitir(@Param("id") Short id);
}
