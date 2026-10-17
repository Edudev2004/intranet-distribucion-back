package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Inventario;
import com.dwi.intranetdistribucion.model.InventarioId;
import com.dwi.intranetdistribucion.repository.projection.InventarioDetalleView;
import com.dwi.intranetdistribucion.repository.projection.InventarioReporteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, InventarioId> {

    Optional<Inventario> findByIdAlmacenAndIdProducto(Integer idAlmacen, Integer idProducto);

    List<Inventario> findByIdAlmacen(Integer idAlmacen);

    List<Inventario> findByIdProducto(Integer idProducto);

    @Query(value = """
            SELECT a.id_almacen AS "idAlmacen",
                   a.nombre AS "almacen",
                   s.nombre AS "sede",
                   p.id_producto AS "idProducto",
                   p.codigo AS "codigo",
                   p.nombre AS "producto",
                   c.nombre AS "categoria",
                   um.codigo AS "unidad",
                   i.stock_actual AS "stockActual",
                   p.stock_minimo AS "stockMinimo"
            FROM inventario i
            JOIN producto p ON p.id_producto = i.id_producto
            JOIN categoria c ON c.id_categoria = p.id_categoria
            JOIN unidad_medida um ON um.id_unidad = p.id_unidad
            JOIN almacen a ON a.id_almacen = i.id_almacen
            JOIN sede s ON s.id_sede = a.id_sede
            WHERE (CAST(:idAlmacen AS integer) IS NULL OR i.id_almacen = :idAlmacen)
              AND (CAST(:idCategoria AS integer) IS NULL OR p.id_categoria = :idCategoria)
            ORDER BY a.nombre, c.nombre, p.nombre
            """, nativeQuery = true)
    List<InventarioReporteView> obtenerReporte(@Param("idAlmacen") Integer idAlmacen,
                                               @Param("idCategoria") Integer idCategoria);

    @Query(value = """
            SELECT a.id_almacen AS "idAlmacen",
                   a.nombre AS "almacen",
                   s.nombre AS "sede",
                   p.id_producto AS "idProducto",
                   p.codigo AS "codigo",
                   p.nombre AS "producto",
                   c.nombre AS "categoria",
                   um.codigo AS "unidad",
                   i.stock_actual AS "stockActual",
                   p.stock_minimo AS "stockMinimo",
                   p.precio_unitario AS "precioUnitario"
            FROM inventario i
            JOIN producto p ON p.id_producto = i.id_producto
            JOIN categoria c ON c.id_categoria = p.id_categoria
            JOIN unidad_medida um ON um.id_unidad = p.id_unidad
            JOIN almacen a ON a.id_almacen = i.id_almacen
            JOIN sede s ON s.id_sede = a.id_sede
            WHERE (CAST(:idAlmacen AS integer) IS NULL OR i.id_almacen = :idAlmacen)
              AND (CAST(:idCategoria AS integer) IS NULL OR p.id_categoria = :idCategoria)
              AND (:soloBajoStock IS FALSE OR i.stock_actual <= p.stock_minimo)
              AND (CAST(:buscar AS varchar) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :buscar, '%')))
            ORDER BY a.nombre, c.nombre, p.nombre
            """, nativeQuery = true)
    List<InventarioDetalleView> consultarInventario(
            @Param("idAlmacen") Integer idAlmacen,
            @Param("idCategoria") Integer idCategoria,
            @Param("soloBajoStock") boolean soloBajoStock,
            @Param("buscar") String buscar
    );

    @Query(value = """
            SELECT a.id_almacen AS "idAlmacen",
                   a.nombre AS "almacen",
                   s.nombre AS "sede",
                   p.id_producto AS "idProducto",
                   p.codigo AS "codigo",
                   p.nombre AS "producto",
                   c.nombre AS "categoria",
                   um.codigo AS "unidad",
                   i.stock_actual AS "stockActual",
                   p.stock_minimo AS "stockMinimo",
                   p.precio_unitario AS "precioUnitario"
            FROM inventario i
            JOIN producto p ON p.id_producto = i.id_producto
            JOIN categoria c ON c.id_categoria = p.id_categoria
            JOIN unidad_medida um ON um.id_unidad = p.id_unidad
            JOIN almacen a ON a.id_almacen = i.id_almacen
            JOIN sede s ON s.id_sede = a.id_sede
            WHERE i.stock_actual <= p.stock_minimo
              AND (CAST(:idAlmacen AS integer) IS NULL OR i.id_almacen = :idAlmacen)
              AND (CAST(:idCategoria AS integer) IS NULL OR p.id_categoria = :idCategoria)
            ORDER BY (i.stock_actual - p.stock_minimo) ASC, p.nombre ASC
            """, nativeQuery = true)
    List<InventarioDetalleView> consultarAlertas(
            @Param("idAlmacen") Integer idAlmacen,
            @Param("idCategoria") Integer idCategoria
    );

    @Query(value = "SELECT nombre FROM almacen WHERE id_almacen = :idAlmacen", nativeQuery = true)
    Optional<String> obtenerNombreAlmacen(@Param("idAlmacen") Integer idAlmacen);
}
