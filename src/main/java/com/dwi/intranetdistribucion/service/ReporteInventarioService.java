package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteInventarioDTO;

public interface ReporteInventarioService {

    ReporteInventarioDTO obtenerReporte(Integer idAlmacen, Integer idCategoria);

    ArchivoReporteDTO generarPdf(Integer idAlmacen, Integer idCategoria);

    ArchivoReporteDTO generarExcel(Integer idAlmacen, Integer idCategoria);
}
