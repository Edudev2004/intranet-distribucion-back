package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReportePedidoDTO;
import com.dwi.intranetdistribucion.service.ReportePedidoService;
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
@RequestMapping("/api/v1/reportes/pedidos")
@RequiredArgsConstructor
public class ReportePedidoController {

    private static final String MEDIA_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportePedidoService reportePedidoService;

    @GetMapping
    public ResponseEntity<ReportePedidoDTO> obtenerReporte(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idEstado", required = false) Integer idEstado,
            @RequestParam(name = "idCliente", required = false) Integer idCliente) {
        return ResponseEntity.ok(reportePedidoService.obtenerReporte(fechaInicio, fechaFin, idEstado, idCliente));
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idEstado", required = false) Integer idEstado,
            @RequestParam(name = "idCliente", required = false) Integer idCliente) {
        return construirDescarga(reportePedidoService.generarPdf(fechaInicio, fechaFin, idEstado, idCliente),
                MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = "/excel", produces = MEDIA_TYPE_EXCEL)
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "idEstado", required = false) Integer idEstado,
            @RequestParam(name = "idCliente", required = false) Integer idCliente) {
        return construirDescarga(reportePedidoService.generarExcel(fechaInicio, fechaFin, idEstado, idCliente),
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
