package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Inventario;
import com.dwi.intranetdistribucion.model.InventarioId;
import com.dwi.intranetdistribucion.repository.projection.InventarioReporteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Consultas nativas: producto y almacén pertenecen a otros módulos
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, InventarioId> {

    // Los CAST permiten filtros opcionales: PostgreSQL no infiere el tipo de un parámetro nulo
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

    @Query(value = "SELECT nombre FROM almacen WHERE id_almacen = :idAlmacen", nativeQuery = true)
    Optional<String> obtenerNombreAlmacen(@Param("idAlmacen") Integer idAlmacen);
}
