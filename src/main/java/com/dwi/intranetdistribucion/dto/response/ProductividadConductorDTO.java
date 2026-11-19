package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductividadConductorDTO {

    private Integer idConductor;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String dni;
    private String nroLicencia;
    private Integer rutasAsignadas;
    private Integer rutasCompletadas;
    private Long tiempoTotalMinutos;
    private Double tiempoPromedioMinutos;
    private String tiempoPromedioFormateado;
    private Double tasaCumplimientoPct;
}
