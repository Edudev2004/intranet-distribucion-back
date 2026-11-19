package com.dwi.intranetdistribucion.repository.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface ProgramacionReporteView {

    Integer getIdProgramacion();

    Integer getIdRuta();

    String getRuta();

    Integer getIdVehiculo();

    String getVehiculoPlaca();

    String getVehiculoMarca();

    String getVehiculoModelo();

    BigDecimal getVehiculoCapacidadKg();

    Integer getIdConductor();

    String getConductorNombres();

    String getConductorApellidos();

    String getConductorDni();

    String getConductorLicencia();

    OffsetDateTime getInicioProgramado();

    OffsetDateTime getFinProgramado();

    OffsetDateTime getInicioReal();

    OffsetDateTime getFinReal();

    Short getIdEstadoProgramacion();

    String getEstadoProgramacion();
}
