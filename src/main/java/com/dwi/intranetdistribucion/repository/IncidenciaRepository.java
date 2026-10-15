package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Incidencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    @Override
    @EntityGraph(attributePaths = {"entrega", "tipoIncidencia", "usuario"})
    Optional<Incidencia> findById(Long id);

    @EntityGraph(attributePaths = {"entrega", "tipoIncidencia", "usuario"})
    List<Incidencia> findAllByOrderByFechaHoraDesc();

    @EntityGraph(attributePaths = {"entrega", "tipoIncidencia", "usuario"})
    List<Incidencia> findByEntregaIdEntregaOrderByFechaHoraDesc(Long idEntrega);

    @EntityGraph(attributePaths = {"entrega", "tipoIncidencia", "usuario"})
    List<Incidencia> findByEntregaIdPedidoOrderByFechaHoraDesc(Long idPedido);
}
