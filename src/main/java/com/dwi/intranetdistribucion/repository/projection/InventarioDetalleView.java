package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;

public interface InventarioDetalleView {

    Integer getIdAlmacen();

    String getAlmacen();

    String getSede();

    Integer getIdProducto();

    String getCodigo();

    String getProducto();

    String getCategoria();

    String getUnidad();

    BigDecimal getStockActual();

    BigDecimal getStockMinimo();

    BigDecimal getPrecioUnitario();
}
