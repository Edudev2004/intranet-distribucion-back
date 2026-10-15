package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Almacen;
import com.dwi.intranetdistribucion.repository.projection.ProductoAlmacenView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Integer> {

    List<Almacen> findAllByOrderByNombreAsc();

    List<Almacen> findBySedeIdSedeOrderByNombreAsc(Integer idSede);

    boolean existsBySedeIdSedeAndNombreIgnoreCase(Integer idSede, String nombre);

    boolean existsBySedeIdSedeAndNombreIgnoreCaseAndIdAlmacenNot(Integer idSede, String nombre, Integer idAlmacen);

    @Query(value = """
            SELECT p.id_producto AS "idProducto",
                   p.codigo AS "codigo",
                   p.nombre AS "producto",
                   c.nombre AS "categoria",
                   i.stock_actual AS "stockActual"
            FROM inventario i
            JOIN producto p ON p.id_producto = i.id_producto
            JOIN categoria c ON c.id_categoria = p.id_categoria
            WHERE i.id_almacen = :idAlmacen
            ORDER BY p.nombre
            """, nativeQuery = true)
    List<ProductoAlmacenView> obtenerProductos(@Param("idAlmacen") Integer idAlmacen);

    @Query(value = """
            SELECT (SELECT COUNT(*) FROM inventario WHERE id_almacen = :idAlmacen)
                 + (SELECT COUNT(*) FROM pedido WHERE id_almacen = :idAlmacen)
                 + (SELECT COUNT(*) FROM movimiento_inventario WHERE id_almacen = :idAlmacen)
            """, nativeQuery = true)
    long contarDependencias(@Param("idAlmacen") Integer idAlmacen);
}