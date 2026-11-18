package com.dwi.intranetdistribucion.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class ProgramacionRutaResponseDTO {
    private Integer idProgramacion;
    private Integer idRuta;
    private Integer idVehiculo;
    private Integer idConductor;
    private OffsetDateTime inicioProgramado;
    private OffsetDateTime finProgramado;
    private OffsetDateTime inicioReal;
    private OffsetDateTime finReal;
    private Short idEstadoProgramacion;
    private String estadoProgramacion;
}