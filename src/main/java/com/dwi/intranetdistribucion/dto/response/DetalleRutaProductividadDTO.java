package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRutaProductividadDTO {

    private Integer idProgramacion;
    private String ruta;
    private String vehiculoPlaca;
    private String conductorNombre;
    private OffsetDateTime inicioProgramado;
    private OffsetDateTime finProgramado;
    private OffsetDateTime inicioReal;
    private OffsetDateTime finReal;
    private Long duracionMinutos;
    private String duracionFormateada;
    private Short idEstadoProgramacion;
    private String estadoProgramacion;
    private Boolean completada;
}
