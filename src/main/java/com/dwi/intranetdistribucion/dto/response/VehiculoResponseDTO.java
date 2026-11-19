package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoResponseDTO {

    private Integer idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private BigDecimal capacidadKg;
    private Short idEstadoVehiculo;
    private String nombreEstado;
}
