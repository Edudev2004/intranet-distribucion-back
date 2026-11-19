package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface PedidoDisponibleAsignacionView {

    Long getIdPedido();

    Integer getIdCliente();

    String getCliente();

    String getNumeroDocumentoCliente();

    String getDireccionEntrega();

    String getZonaCliente();

    Integer getIdAlmacen();

    String getAlmacen();

    Short getIdEstadoPedido();

    String getEstado();

    OffsetDateTime getFechaPedido();

    LocalDate getFechaEntregaSolicitada();

    BigDecimal getPesoTotalKg();

    BigDecimal getImporteTotal();
}
