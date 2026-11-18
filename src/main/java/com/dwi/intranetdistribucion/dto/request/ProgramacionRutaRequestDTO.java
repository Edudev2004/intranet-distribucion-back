package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ProgramacionRutaRequestDTO {

    @NotNull(message = "La ruta es obligatoria")
    private Integer idRuta;

    @NotNull(message = "El vehículo es obligatorio")
    private Integer idVehiculo;

    @NotNull(message = "El conductor es obligatorio")
    private Integer idConductor;

    @NotNull(message = "El inicio programado es obligatorio")
    private OffsetDateTime inicioProgramado;

    @NotNull(message = "El fin programado es obligatorio")
    private OffsetDateTime finProgramado;
}