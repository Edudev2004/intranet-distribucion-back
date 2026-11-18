package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.AprobacionPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AprobacionPedidoRepository extends JpaRepository<AprobacionPedido, Long> {
    boolean existsByPedidoIdPedido(Long idPedido);
    Optional<AprobacionPedido> findByPedidoIdPedido(Long idPedido);
}