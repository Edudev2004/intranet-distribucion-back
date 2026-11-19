package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.ConductorRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ConductorResponseDTO;
import com.dwi.intranetdistribucion.service.ConductorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conductores")
@RequiredArgsConstructor
public class ConductorController {

    private final ConductorService conductorService;

    @GetMapping
    public ResponseEntity<List<ConductorResponseDTO>> listar(
            @RequestParam(name = "activos", required = false) Boolean soloActivos) {
        return ResponseEntity.ok(conductorService.listar(soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorResponseDTO> obtenerPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(conductorService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ConductorResponseDTO> crear(@Valid @RequestBody ConductorRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conductorService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConductorResponseDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ConductorRequestDTO request) {
        return ResponseEntity.ok(conductorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable("id") Integer id,
            @RequestParam("activo") Boolean activo) {
        conductorService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}
