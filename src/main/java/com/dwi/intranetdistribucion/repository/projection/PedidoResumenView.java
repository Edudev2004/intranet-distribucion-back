package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;

public interface PedidoResumenView {

    Long getIdPedido();

    String getCliente();

    String getAlmacen();

    Integer getIdEstadoPedido();

    String getEstado();

    String getFechaPedido();

    BigDecimal getTotal();
}