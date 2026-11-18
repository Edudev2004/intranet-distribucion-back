package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;

public interface ProductoDisponibleView {

    Integer getIdProducto();

    String getCodigo();

    String getProducto();

    BigDecimal getPrecioUnitario();

    BigDecimal getStockActual();
}