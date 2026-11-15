package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MantenimientoResponseDTO {

    private Integer idMantenimiento;
    private Integer idVehiculo;
    private String placa;
    private String estadoVehiculo;
    private Short idTipoMantenimiento;
    private String tipoMantenimiento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String descripcion;
    private BigDecimal costo;
    private Boolean vigente;
}