package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteEntregaDTO;
import com.dwi.intranetdistribucion.service.ReporteEntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes/entregas")
@RequiredArgsConstructor
public class ReporteEntregaController {

    private static final String MEDIA_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReporteEntregaService reporteEntregaService;

    @GetMapping
    public ResponseEntity<ReporteEntregaDTO> obtenerReporte(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idEstado", required = false) Short idEstado) {
        return ResponseEntity.ok(reporteEntregaService.obtenerReporte(fechaInicio, fechaFin, idEstado));
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idEstado", required = false) Short idEstado) {
        return construirDescarga(reporteEntregaService.generarPdf(fechaInicio, fechaFin, idEstado),
                MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = "/excel", produces = MEDIA_TYPE_EXCEL)
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idEstado", required = false) Short idEstado) {
        return construirDescarga(reporteEntregaService.generarExcel(fechaInicio, fechaFin, idEstado),
                MediaType.parseMediaType(MEDIA_TYPE_EXCEL));
    }

    private ResponseEntity<byte[]> construirDescarga(ArchivoReporteDTO archivo, MediaType tipo) {
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(archivo.getNombreArchivo())
                .build();

        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentType(tipo);
        cabeceras.setContentDisposition(disposicion);

        return ResponseEntity.ok()
                .headers(cabeceras)
                .body(archivo.getContenido());
    }
}
