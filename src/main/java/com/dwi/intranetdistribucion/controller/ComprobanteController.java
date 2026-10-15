package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.ComprobanteRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ComprobantePdfDTO;
import com.dwi.intranetdistribucion.dto.response.ComprobanteResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoComprobanteResponseDTO;
import com.dwi.intranetdistribucion.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @PostMapping
    public ResponseEntity<ComprobanteResponseDTO> emitir(@Valid @RequestBody ComprobanteRequestDTO comprobanteRequestDTO) {
        return new ResponseEntity<>(comprobanteService.emitir(comprobanteRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ComprobanteResponseDTO>> listar(
            @RequestParam(required = false) Long idPedido,
            @RequestParam(required = false) Short idTipoComprobante) {
        return ResponseEntity.ok(comprobanteService.listar(idPedido, idTipoComprobante));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comprobanteService.obtenerPorId(id));
    }

    // descargar=false muestra el PDF en el navegador en lugar de descargarlo
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean descargar) {
        ComprobantePdfDTO pdf = comprobanteService.generarPdf(id);
        ContentDisposition disposicion = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(pdf.getNombreArchivo())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.getContenido().length)
                .body(pdf.getContenido());
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoComprobanteResponseDTO>> listarTipos() {
        return ResponseEntity.ok(comprobanteService.listarTipos());
    }
}
