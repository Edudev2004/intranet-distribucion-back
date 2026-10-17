package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.MovimientoInventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = {"almacen", "tipoMovimiento", "usuario", "detalles", "detalles.producto"})
    @Query("SELECT m FROM MovimientoInventario m ORDER BY m.fecha DESC")
    List<MovimientoInventario> findAllWithDetalles();

    @EntityGraph(attributePaths = {"almacen", "tipoMovimiento", "usuario", "detalles", "detalles.producto"})
    @Query("SELECT m FROM MovimientoInventario m WHERE m.idMovimiento = :id")
    Optional<MovimientoInventario> findByIdWithDetalles(@Param("id") Long id);

    @EntityGraph(attributePaths = {"almacen", "tipoMovimiento", "usuario", "detalles", "detalles.producto"})
    @Query("SELECT m FROM MovimientoInventario m WHERE m.almacen.idAlmacen = :idAlmacen ORDER BY m.fecha DESC")
    List<MovimientoInventario> findByAlmacenIdAlmacenOrderByFechaDesc(@Param("idAlmacen") Integer idAlmacen);

    @Query(value = """
            SELECT DISTINCT m.*
            FROM movimiento_inventario m
            LEFT JOIN detalle_movimiento dm ON dm.id_movimiento = m.id_movimiento
            WHERE (CAST(:idAlmacen AS integer) IS NULL OR m.id_almacen = :idAlmacen)
              AND (CAST(:idProducto AS integer) IS NULL OR dm.id_producto = :idProducto)
              AND (CAST(:idTipoMovimiento AS smallint) IS NULL OR m.id_tipo_movimiento = :idTipoMovimiento)
              AND (CAST(:fechaInicio AS timestamp with time zone) IS NULL OR m.fecha >= :fechaInicio)
              AND (CAST(:fechaFin AS timestamp with time zone) IS NULL OR m.fecha <= :fechaFin)
            ORDER BY m.fecha DESC
            """, nativeQuery = true)
    List<MovimientoInventario> buscarConFiltros(
            @Param("idAlmacen") Integer idAlmacen,
            @Param("idProducto") Integer idProducto,
            @Param("idTipoMovimiento") Short idTipoMovimiento,
            @Param("fechaInicio") OffsetDateTime fechaInicio,
            @Param("fechaFin") OffsetDateTime fechaFin
    );
}
