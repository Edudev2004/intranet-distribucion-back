package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteEntregaDTO;

import java.time.LocalDate;

public interface ReporteEntregaService {

    ReporteEntregaDTO obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin, Short idEstado);

    ArchivoReporteDTO generarPdf(LocalDate fechaInicio, LocalDate fechaFin, Short idEstado);

    ArchivoReporteDTO generarExcel(LocalDate fechaInicio, LocalDate fechaFin, Short idEstado);
}
