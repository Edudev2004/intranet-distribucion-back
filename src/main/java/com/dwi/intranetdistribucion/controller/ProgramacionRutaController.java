package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.ProgramacionRutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ProgramacionRutaResponseDTO;
import com.dwi.intranetdistribucion.service.ProgramacionRutaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/programacion-rutas")
@RequiredArgsConstructor
public class ProgramacionRutaController {

    private final ProgramacionRutaService programacionRutaService;

    @GetMapping
    public ResponseEntity<List<ProgramacionRutaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(programacionRutaService.listarTodas());
    }

    @PostMapping
    public ResponseEntity<ProgramacionRutaResponseDTO> registrarAsignacion(
            @Valid @RequestBody ProgramacionRutaRequestDTO request) {
        return ResponseEntity.status(201).body(programacionRutaService.registrarAsignacion(request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProgramacionRutaResponseDTO> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam Short idEstado) {
        return ResponseEntity.ok(programacionRutaService.actualizarEstado(id, idEstado));
    }
}