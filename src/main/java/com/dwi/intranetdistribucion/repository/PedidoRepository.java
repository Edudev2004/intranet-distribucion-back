package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Pedido;
import com.dwi.intranetdistribucion.repository.projection.ClienteOpcionView;
import com.dwi.intranetdistribucion.repository.projection.PedidoCabeceraView;
import com.dwi.intranetdistribucion.repository.projection.PedidoDetalleView;
import com.dwi.intranetdistribucion.repository.projection.PedidoResumenView;
import com.dwi.intranetdistribucion.repository.projection.ProductoDisponibleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Consultas nativas: cliente, almacén, producto y transporte pertenecen a otros módulos
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    long countByIdEstadoPedido(Short idEstadoPedido);

    @Query(value = """
            SELECT p.id_pedido AS "idPedido",
                   to_char(p.fecha_pedido AT TIME ZONE 'America/Lima', 'DD/MM/YYYY HH24:MI') AS "fechaPedido",
                   to_char(p.fecha_entrega_solicitada, 'DD/MM/YYYY') AS "fechaEntregaSolicitada",
                   p.observacion AS "observacion",
                   ep.nombre AS "estadoPedido",
                   td.nombre AS "tipoDocumentoCliente",
                   c.numero_documento AS "numeroDocumentoCliente",
                   c.nombre_razon_social AS "cliente",
                   c.direccion AS "direccionCliente",
                   c.telefono AS "telefonoCliente",
                   c.correo AS "correoCliente",
                   a.nombre AS "almacen",
                   s.nombre AS "sede",
                   s.direccion AS "direccionSede",
                   t.placa AS "placaVehiculo",
                   t.conductor AS "conductor",
                   t.nro_licencia AS "licenciaConductor"
            FROM pedido p
            JOIN cliente c ON c.id_cliente = p.id_cliente
            JOIN tipo_documento td ON td.id_tipo_documento = c.id_tipo_documento
            JOIN almacen a ON a.id_almacen = p.id_almacen
            JOIN sede s ON s.id_sede = a.id_sede
            JOIN estado_pedido ep ON ep.id_estado_pedido = p.id_estado_pedido
            LEFT JOIN LATERAL (
                SELECT v.placa,
                       co.nombres || ' ' || co.apellidos AS conductor,
                       co.nro_licencia
                FROM entrega e
                JOIN programacion_ruta pr ON pr.id_programacion = e.id_programacion
                JOIN vehiculo v ON v.id_vehiculo = pr.id_vehiculo
                JOIN conductor co ON co.id_conductor = pr.id_conductor
                WHERE e.id_pedido = p.id_pedido
                ORDER BY e.id_entrega DESC
                LIMIT 1
            ) t ON TRUE
            WHERE p.id_pedido = :idPedido
            """, nativeQuery = true)
    Optional<PedidoCabeceraView> obtenerCabecera(@Param("idPedido") Long idPedido);

    @Query(value = """
            SELECT pr.codigo AS "codigo",
                   pr.nombre AS "producto",
                   um.codigo AS "unidad",
                   dp.cantidad AS "cantidad",
                   dp.precio_unitario AS "precioUnitario",
                   dp.cantidad * dp.precio_unitario AS "subtotal",
                   dp.cantidad * pr.peso_kg AS "pesoKg"
            FROM detalle_pedido dp
            JOIN producto pr ON pr.id_producto = dp.id_producto
            JOIN unidad_medida um ON um.id_unidad = pr.id_unidad
            WHERE dp.id_pedido = :idPedido
            ORDER BY pr.nombre
            """, nativeQuery = true)
    List<PedidoDetalleView> obtenerDetalle(@Param("idPedido") Long idPedido);

    @Query(value = """
            SELECT p.id_pedido AS "idPedido",
                   c.nombre_razon_social AS "cliente",
                   a.nombre AS "almacen",
                   CAST(p.id_estado_pedido AS integer) AS "idEstadoPedido",
                   ep.nombre AS "estado",
                   to_char(p.fecha_pedido AT TIME ZONE 'America/Lima', 'DD/MM/YYYY HH24:MI') AS "fechaPedido",
                   COALESCE(SUM(dp.cantidad * dp.precio_unitario), 0) AS "total"
            FROM pedido p
            JOIN cliente c ON c.id_cliente = p.id_cliente
            JOIN almacen a ON a.id_almacen = p.id_almacen
            JOIN estado_pedido ep ON ep.id_estado_pedido = p.id_estado_pedido
            LEFT JOIN detalle_pedido dp ON dp.id_pedido = p.id_pedido
            WHERE (CAST(:idEstado AS integer) IS NULL OR p.id_estado_pedido = :idEstado)
            GROUP BY p.id_pedido, c.nombre_razon_social, a.nombre, p.id_estado_pedido, ep.nombre, p.fecha_pedido
            ORDER BY p.id_pedido DESC
            """, nativeQuery = true)
    List<PedidoResumenView> listarResumen(@Param("idEstado") Integer idEstado);

    @Query(value = """
            SELECT p.id_pedido AS "idPedido",
                   c.nombre_razon_social AS "cliente",
                   a.nombre AS "almacen",
                   CAST(p.id_estado_pedido AS integer) AS "idEstadoPedido",
                   ep.nombre AS "estado",
                   to_char(p.fecha_pedido AT TIME ZONE 'America/Lima', 'DD/MM/YYYY HH24:MI') AS "fechaPedido",
                   COALESCE(SUM(dp.cantidad * dp.precio_unitario), 0) AS "total"
            FROM pedido p
            JOIN cliente c ON c.id_cliente = p.id_cliente
            JOIN almacen a ON a.id_almacen = p.id_almacen
            JOIN estado_pedido ep ON ep.id_estado_pedido = p.id_estado_pedido
            LEFT JOIN detalle_pedido dp ON dp.id_pedido = p.id_pedido
            WHERE (CAST(:idEstado AS integer) IS NULL OR p.id_estado_pedido = :idEstado)
              AND (CAST(:idCliente AS integer) IS NULL OR p.id_cliente = :idCliente)
              AND (CAST(:fechaInicio AS date) IS NULL OR CAST(p.fecha_pedido AT TIME ZONE 'America/Lima' AS date) >= :fechaInicio)
              AND (CAST(:fechaFin AS date) IS NULL OR CAST(p.fecha_pedido AT TIME ZONE 'America/Lima' AS date) <= :fechaFin)
            GROUP BY p.id_pedido, c.nombre_razon_social, a.nombre, p.id_estado_pedido, ep.nombre, p.fecha_pedido
            ORDER BY p.id_pedido DESC
            """, nativeQuery = true)
    List<PedidoResumenView> obtenerReportePedidos(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin,
            @Param("idEstado") Integer idEstado,
            @Param("idCliente") Integer idCliente);

    @Query(value = """
            SELECT c.id_cliente AS "idCliente",
                   c.nombre_razon_social AS "nombre",
                   c.numero_documento AS "documento"
            FROM cliente c
            WHERE c.activo = TRUE
            ORDER BY c.nombre_razon_social
            """, nativeQuery = true)
    List<ClienteOpcionView> listarClientes();

    @Query(value = "SELECT nombre_razon_social FROM cliente WHERE id_cliente = :idCliente AND activo = TRUE",
            nativeQuery = true)
    Optional<String> obtenerNombreClienteActivo(@Param("idCliente") Integer idCliente);

    @Query(value = "SELECT nombre_razon_social FROM cliente WHERE id_cliente = :idCliente", nativeQuery = true)
    Optional<String> obtenerNombreCliente(@Param("idCliente") Integer idCliente);

    @Query(value = "SELECT nombre FROM estado_pedido WHERE id_estado_pedido = :idEstado", nativeQuery = true)
    Optional<String> obtenerNombreEstado(@Param("idEstado") Integer idEstado);

    @Query(value = """
            SELECT p.id_producto AS "idProducto",
                   p.codigo AS "codigo",
                   p.nombre AS "producto",
                   p.precio_unitario AS "precioUnitario",
                   i.stock_actual AS "stockActual"
            FROM inventario i
            JOIN producto p ON p.id_producto = i.id_producto
            WHERE i.id_almacen = :idAlmacen
              AND p.activo = TRUE
              AND i.stock_actual > 0
            ORDER BY p.nombre
            """, nativeQuery = true)
    List<ProductoDisponibleView> listarProductosDisponibles(@Param("idAlmacen") Integer idAlmacen);

    @Query(value = """
            SELECT p.id_pedido AS "idPedido",
                   p.id_cliente AS "idCliente",
                   c.nombre_razon_social AS "cliente",
                   c.numero_documento AS "numeroDocumentoCliente",
                   c.direccion AS "direccionEntrega",
                   z.nombre AS "zonaCliente",
                   p.id_almacen AS "idAlmacen",
                   a.nombre AS "almacen",
                   CAST(p.id_estado_pedido AS smallint) AS "idEstadoPedido",
                   ep.nombre AS "estado",
                   p.fecha_pedido AS "fechaPedido",
                   p.fecha_entrega_solicitada AS "fechaEntregaSolicitada",
                   COALESCE(SUM(dp.cantidad * COALESCE(prod.peso_kg, 0)), 0) AS "pesoTotalKg",
                   COALESCE(SUM(dp.cantidad * dp.precio_unitario), 0) AS "importeTotal"
            FROM pedido p
            JOIN cliente c ON c.id_cliente = p.id_cliente
            LEFT JOIN zona z ON z.id_zona = c.id_zona
            JOIN almacen a ON a.id_almacen = p.id_almacen
            JOIN estado_pedido ep ON ep.id_estado_pedido = p.id_estado_pedido
            LEFT JOIN detalle_pedido dp ON dp.id_pedido = p.id_pedido
            LEFT JOIN producto prod ON prod.id_producto = dp.id_producto
            WHERE p.id_estado_pedido = 2
              AND p.id_pedido NOT IN (
                  SELECT e.id_pedido FROM entrega e WHERE e.id_estado_entrega IN (1, 2, 3)
              )
              AND (CAST(:idAlmacen AS integer) IS NULL OR p.id_almacen = :idAlmacen)
              AND (CAST(:idZona AS integer) IS NULL OR c.id_zona = :idZona)
            GROUP BY p.id_pedido, p.id_cliente, c.nombre_razon_social, c.numero_documento, c.direccion, z.nombre, p.id_almacen, a.nombre, p.id_estado_pedido, ep.nombre, p.fecha_pedido, p.fecha_entrega_solicitada
            ORDER BY p.id_pedido ASC
            """, nativeQuery = true)
    List<com.dwi.intranetdistribucion.repository.projection.PedidoDisponibleAsignacionView> listarPedidosDisponiblesParaRuta(
            @Param("idAlmacen") Integer idAlmacen,
            @Param("idZona") Integer idZona);
}