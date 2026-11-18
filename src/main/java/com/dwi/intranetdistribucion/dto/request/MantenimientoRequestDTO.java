package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MantenimientoRequestDTO {

    @NotNull(message = "El vehículo es obligatorio")
    private Integer idVehiculo;

    @NotNull(message = "El tipo de mantenimiento es obligatorio")
    private Short idTipoMantenimiento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @Size(max = 255, message = "La descripción no debe superar 255 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal costo;
}