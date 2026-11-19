package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductividadVehiculoDTO {

    private Integer idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private BigDecimal capacidadKg;
    private Integer rutasAsignadas;
    private Integer rutasCompletadas;
    private Long tiempoTotalMinutos;
    private Double tiempoPromedioMinutos;
    private String tiempoPromedioFormateado;
    private Double tasaCumplimientoPct;
}
