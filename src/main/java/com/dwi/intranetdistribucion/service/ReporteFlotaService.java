package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteProductividadFlotaDTO;

import java.time.LocalDate;

public interface ReporteFlotaService {

    ReporteProductividadFlotaDTO obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin, Integer idVehiculo, Integer idConductor);

    ArchivoReporteDTO generarPdf(LocalDate fechaInicio, LocalDate fechaFin, Integer idVehiculo, Integer idConductor);

    ArchivoReporteDTO generarExcel(LocalDate fechaInicio, LocalDate fechaFin, Integer idVehiculo, Integer idConductor);
}
