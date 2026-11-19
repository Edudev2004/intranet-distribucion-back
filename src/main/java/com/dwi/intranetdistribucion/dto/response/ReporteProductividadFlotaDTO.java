package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteProductividadFlotaDTO {

    private Integer totalRutasProgramadas;
    private Integer totalRutasCompletadas;
    private Double tasaCumplimientoGeneralPct;
    private Double tiempoPromedioGeneralMinutos;
    private String tiempoPromedioGeneralFormateado;
    private String vehiculoMasProductivo;
    private String conductorMasProductivo;
    private List<ProductividadVehiculoDTO> metricasVehiculos;
    private List<ProductividadConductorDTO> metricasConductores;
    private List<DetalleRutaProductividadDTO> detallesRutas;
}
