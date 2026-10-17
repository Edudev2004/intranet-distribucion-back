package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteInventarioDTO;
import com.dwi.intranetdistribucion.service.ReporteInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes/inventario")
@RequiredArgsConstructor
public class ReporteInventarioController {

    private static final String MEDIA_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReporteInventarioService reporteInventarioService;

    // Vista previa en JSON con los mismos datos que los archivos exportados
    @GetMapping
    public ResponseEntity<ReporteInventarioDTO> obtenerReporte(
            @RequestParam(name = "idAlmacen", required = false) Integer idAlmacen,
            @RequestParam(name = "idCategoria", required = false) Integer idCategoria) {
        return ResponseEntity.ok(reporteInventarioService.obtenerReporte(idAlmacen, idCategoria));
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(name = "idAlmacen", required = false) Integer idAlmacen,
            @RequestParam(name = "idCategoria", required = false) Integer idCategoria) {
        return construirDescarga(reporteInventarioService.generarPdf(idAlmacen, idCategoria),
                MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = "/excel", produces = MEDIA_TYPE_EXCEL)
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(name = "idAlmacen", required = false) Integer idAlmacen,
            @RequestParam(name = "idCategoria", required = false) Integer idCategoria) {
        return construirDescarga(reporteInventarioService.generarExcel(idAlmacen, idCategoria),
                MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
    }

    private ResponseEntity<byte[]> construirDescarga(ArchivoReporteDTO archivo, MediaType tipo) {
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(archivo.getNombreArchivo())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentType(tipo)
                .contentLength(archivo.getContenido().length)
                .body(archivo.getContenido());
    }
}
