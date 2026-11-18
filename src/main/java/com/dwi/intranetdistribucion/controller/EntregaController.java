package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.ActualizarEntregaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EntregaResponseDTO;
import com.dwi.intranetdistribucion.service.EntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;

    @GetMapping("/programacion/{idProgramacion}")
    @PreAuthorize("hasAuthority('Entregas_ver')")
    public ResponseEntity<List<EntregaResponseDTO>> listarPorProgramacion(@PathVariable Integer idProgramacion) {
        return ResponseEntity.ok(entregaService.listarPorProgramacion(idProgramacion));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('Entregas_actualizar')")
    public ResponseEntity<EntregaResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEntregaRequestDTO request) {
        return ResponseEntity.ok(entregaService.actualizarEstadoEntrega(id, request));
    }
}