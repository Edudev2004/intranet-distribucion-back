package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Comprobante;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    @Override
    @EntityGraph(attributePaths = {"pedido", "tipoComprobante", "usuario"})
    Optional<Comprobante> findById(Long id);

    @EntityGraph(attributePaths = {"pedido", "tipoComprobante", "usuario"})
    List<Comprobante> findAllByOrderByFechaEmisionDesc();

    @EntityGraph(attributePaths = {"pedido", "tipoComprobante", "usuario"})
    List<Comprobante> findByPedidoIdPedidoOrderByFechaEmisionDesc(Long idPedido);

    @EntityGraph(attributePaths = {"pedido", "tipoComprobante", "usuario"})
    List<Comprobante> findByTipoComprobanteIdTipoComprobanteOrderByFechaEmisionDesc(Short idTipoComprobante);

    @EntityGraph(attributePaths = {"pedido", "tipoComprobante", "usuario"})
    List<Comprobante> findByPedidoIdPedidoAndTipoComprobanteIdTipoComprobanteOrderByFechaEmisionDesc(
            Long idPedido, Short idTipoComprobante);

    boolean existsByPedidoIdPedidoAndTipoComprobanteIdTipoComprobante(Long idPedido, Short idTipoComprobante);

    @Query("SELECT COALESCE(MAX(c.numero), 0) FROM Comprobante c "
            + "WHERE c.tipoComprobante.idTipoComprobante = :idTipo AND c.serie = :serie")
    int obtenerUltimoNumero(@Param("idTipo") Short idTipoComprobante, @Param("serie") String serie);
}
