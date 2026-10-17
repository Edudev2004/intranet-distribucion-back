package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.IncidenciaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.IncidenciaResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoIncidenciaResponseDTO;
import com.dwi.intranetdistribucion.service.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    @PostMapping
    public ResponseEntity<IncidenciaResponseDTO> registrar(@Valid @RequestBody IncidenciaRequestDTO incidenciaRequestDTO) {
        return new ResponseEntity<>(incidenciaService.registrar(incidenciaRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<IncidenciaResponseDTO>> listar(
            @RequestParam(name = "idEntrega", required = false) Long idEntrega,
            @RequestParam(name = "idPedido", required = false) Long idPedido) {
        return ResponseEntity.ok(incidenciaService.listar(idEntrega, idPedido));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponseDTO> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoIncidenciaResponseDTO>> listarTipos() {
        return ResponseEntity.ok(incidenciaService.listarTipos());
    }
}
