package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdProductoNot(String codigo, Integer idProducto);

    // Consultas nativas: inventario y detalle_pedido pertenecen a otros módulos
    @Query(value = "SELECT COUNT(*) FROM inventario WHERE id_producto = :idProducto", nativeQuery = true)
    long contarRegistrosInventario(@Param("idProducto") Integer idProducto);

    @Query(value = "SELECT COUNT(*) FROM detalle_pedido WHERE id_producto = :idProducto", nativeQuery = true)
    long contarPedidosAsociados(@Param("idProducto") Integer idProducto);
}
