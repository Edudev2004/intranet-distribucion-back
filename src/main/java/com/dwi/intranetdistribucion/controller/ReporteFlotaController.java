package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteProductividadFlotaDTO;
import com.dwi.intranetdistribucion.service.ReporteFlotaService;
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
@RequestMapping("/api/v1/reportes/flota")
@RequiredArgsConstructor
public class ReporteFlotaController {

    private static final String MEDIA_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReporteFlotaService reporteFlotaService;

    @GetMapping
    public ResponseEntity<ReporteProductividadFlotaDTO> obtenerReporte(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idVehiculo", required = false) Integer idVehiculo,
            @RequestParam(name = "idConductor", required = false) Integer idConductor) {
        return ResponseEntity.ok(reporteFlotaService.obtenerReporte(fechaInicio, fechaFin, idVehiculo, idConductor));
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idVehiculo", required = false) Integer idVehiculo,
            @RequestParam(name = "idConductor", required = false) Integer idConductor) {
        return construirDescarga(reporteFlotaService.generarPdf(fechaInicio, fechaFin, idVehiculo, idConductor),
                MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = "/excel", produces = MEDIA_TYPE_EXCEL)
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idVehiculo", required = false) Integer idVehiculo,
            @RequestParam(name = "idConductor", required = false) Integer idConductor) {
        return construirDescarga(reporteFlotaService.generarExcel(fechaInicio, fechaFin, idVehiculo, idConductor),
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
