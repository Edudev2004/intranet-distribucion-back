package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReportePedidoDTO;

import java.time.LocalDate;

public interface ReportePedidoService {

    ReportePedidoDTO obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin, Integer idEstado, Integer idCliente);

    ArchivoReporteDTO generarPdf(LocalDate fechaInicio, LocalDate fechaFin, Integer idEstado, Integer idCliente);

    ArchivoReporteDTO generarExcel(LocalDate fechaInicio, LocalDate fechaFin, Integer idEstado, Integer idCliente);
}
