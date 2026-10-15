package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;

// Fila del reporte de inventario (inventario + producto + categoría + almacén + sede)
public interface InventarioReporteView {

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
}
