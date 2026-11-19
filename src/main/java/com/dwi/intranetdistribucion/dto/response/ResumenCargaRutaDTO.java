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
public class ResumenCargaRutaDTO {

    private Integer idProgramacion;
    private Integer idVehiculo;
    private String placaVehiculo;
    private String modeloVehiculo;
    private BigDecimal capacidadMaximaKg;
    private BigDecimal pesoOcupadoKg;
    private BigDecimal pesoDisponibleKg;
    private BigDecimal porcentajeOcupacion;
    private Integer totalPedidos;
}
