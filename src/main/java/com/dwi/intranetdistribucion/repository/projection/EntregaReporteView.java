package com.dwi.intranetdistribucion.repository.projection;

import java.time.OffsetDateTime;

public interface EntregaReporteView {

    Long getIdEntrega();

    Long getIdPedido();

    String getCliente();

    String getDireccion();

    String getEstadoEntrega();

    Short getIdEstadoEntrega();

    OffsetDateTime getHoraEstimada();

    OffsetDateTime getHoraEntregaReal();

    String getNotas();
}
