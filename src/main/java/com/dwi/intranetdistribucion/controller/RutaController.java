package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.RutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.RutaResponseDTO;
import com.dwi.intranetdistribucion.service.RutaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rutas")
@RequiredArgsConstructor
public class RutaController {

    private final RutaService rutaService;

    @GetMapping
    public ResponseEntity<List<RutaResponseDTO>> listar(
            @RequestParam(name = "idZona", required = false) Integer idZona,
            @RequestParam(name = "activos", required = false) Boolean soloActivos) {
        return ResponseEntity.ok(rutaService.listar(idZona, soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaResponseDTO> obtenerPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(rutaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<RutaResponseDTO> crear(@Valid @RequestBody RutaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rutaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutaResponseDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody RutaRequestDTO request) {
        return ResponseEntity.ok(rutaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable("id") Integer id,
            @RequestParam("activo") Boolean activo) {
        rutaService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}
