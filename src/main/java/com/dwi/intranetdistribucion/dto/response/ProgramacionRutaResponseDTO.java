package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramacionRutaResponseDTO {
    private Integer idProgramacion;
    private Integer idRuta;
    private String nombreRuta;
    private String nombreZona;
    private Integer idVehiculo;
    private String placaVehiculo;
    private String modeloVehiculo;
    private BigDecimal capacidadVehiculoKg;
    private Integer idConductor;
    private String nombreConductor;
    private String dniConductor;
    private String licenciaConductor;
    private OffsetDateTime inicioProgramado;
    private OffsetDateTime finProgramado;
    private OffsetDateTime inicioReal;
    private OffsetDateTime finReal;
    private Short idEstadoProgramacion;
    private String estadoProgramacion;
    private BigDecimal pesoOcupadoKg;
    private BigDecimal porcentajeOcupacion;
    private Integer totalPedidos;
}
