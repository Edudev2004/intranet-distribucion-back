package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.DetallePedido;
import com.dwi.intranetdistribucion.model.DetallePedidoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, DetallePedidoId> {

    List<DetallePedido> findByIdPedido(Long idPedido);

    void deleteByIdPedido(Long idPedido);
}