package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;

public interface ProductoAlmacenView {

    Integer getIdProducto();

    String getCodigo();

    String getProducto();

    String getCategoria();

    BigDecimal getStockActual();
}