package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByIdProgramacion(Integer idProgramacion);

    @Query("SELECT e.nombre FROM EstadoEntrega e WHERE e.idEstadoEntrega = :idEstado")
    Optional<String> obtenerNombreEstadoEntrega(@Param("idEstado") Integer idEstado);

    @Query(value = """
            SELECT e.id_entrega AS "idEntrega",
                   e.id_pedido AS "idPedido",
                   c.nombre_razon_social AS "cliente",
                   c.direccion AS "direccion",
                   ee.nombre AS "estadoEntrega",
                   e.id_estado_entrega AS "idEstadoEntrega",
                   e.hora_estimada AS "horaEstimada",
                   e.hora_entrega_real AS "horaEntregaReal",
                   e.notas AS "notas"
            FROM entrega e
            JOIN pedido p ON p.id_pedido = e.id_pedido
            JOIN cliente c ON c.id_cliente = p.id_cliente
            JOIN estado_entrega ee ON ee.id_estado_entrega = e.id_estado_entrega
            WHERE (CAST(:idEstado AS smallint) IS NULL OR e.id_estado_entrega = :idEstado)
              AND (CAST(:fechaInicio AS date) IS NULL OR CAST(COALESCE(e.hora_estimada, e.hora_entrega_real) AT TIME ZONE 'America/Lima' AS date) >= :fechaInicio)
              AND (CAST(:fechaFin AS date) IS NULL OR CAST(COALESCE(e.hora_estimada, e.hora_entrega_real) AT TIME ZONE 'America/Lima' AS date) <= :fechaFin)
            ORDER BY e.id_entrega DESC
            """, nativeQuery = true)
    List<com.dwi.intranetdistribucion.repository.projection.EntregaReporteView> obtenerReporteEntregas(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin,
            @Param("idEstado") Short idEstado);
}