package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VehiculoRequestDTO {

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa no puede exceder 20 caracteres")
    private String placa;

    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;

    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    @NotNull(message = "La capacidad en kg es obligatoria")
    @DecimalMin(value = "0.01", message = "La capacidad debe ser mayor a 0 kg")
    private BigDecimal capacidadKg;

    @NotNull(message = "El estado del vehículo es obligatorio")
    private Short idEstadoVehiculo;
}
