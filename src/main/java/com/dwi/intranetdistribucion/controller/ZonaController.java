package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.ZonaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ZonaResponseDTO;
import com.dwi.intranetdistribucion.service.ZonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaService zonaService;

    @GetMapping
    public ResponseEntity<List<ZonaResponseDTO>> listar() {
        return ResponseEntity.ok(zonaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZonaResponseDTO> obtenerPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(zonaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ZonaResponseDTO> crear(@Valid @RequestBody ZonaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZonaResponseDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ZonaRequestDTO request) {
        return ResponseEntity.ok(zonaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        zonaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
