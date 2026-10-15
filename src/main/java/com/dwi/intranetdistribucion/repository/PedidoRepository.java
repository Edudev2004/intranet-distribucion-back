package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Pedido;
import com.dwi.intranetdistribucion.repository.projection.PedidoCabeceraView;
import com.dwi.intranetdistribucion.repository.projection.PedidoDetalleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Consultas nativas: cliente, almacén, producto y transporte pertenecen a otros módulos
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

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
}
