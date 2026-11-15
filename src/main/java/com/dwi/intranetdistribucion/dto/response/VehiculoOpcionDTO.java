package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoOpcionDTO {

    private Integer idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private BigDecimal capacidadKg;
    private String estado;
}