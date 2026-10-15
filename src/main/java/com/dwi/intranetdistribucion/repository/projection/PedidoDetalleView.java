package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;

// Línea de producto del pedido para el documento
public interface PedidoDetalleView {

    String getCodigo();

    String getProducto();

    String getUnidad();

    BigDecimal getCantidad();

    BigDecimal getPrecioUnitario();

    BigDecimal getSubtotal();

    BigDecimal getPesoKg();
}
