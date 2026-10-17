package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.SedeRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ResponsableSedeResponseDTO;
import com.dwi.intranetdistribucion.dto.response.SedeResponseDTO;
import com.dwi.intranetdistribucion.service.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/sedes", "/api/sedes"})
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @GetMapping
    public ResponseEntity<List<SedeResponseDTO>> obtenerTodas(
            @RequestParam(name = "soloActivos", required = false, defaultValue = "false") Boolean soloActivos) {
        return ResponseEntity.ok(sedeService.obtenerTodas(soloActivos));
    }

    @GetMapping("/responsables")
    public ResponseEntity<List<ResponsableSedeResponseDTO>> obtenerResponsablesDisponibles() {
        return ResponseEntity.ok(sedeService.obtenerResponsablesDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SedeResponseDTO> obtenerPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(sedeService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SedeResponseDTO> crear(@Valid @RequestBody SedeRequestDTO request) {
        return new ResponseEntity<>(sedeService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SedeResponseDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SedeRequestDTO request) {
        return ResponseEntity.ok(sedeService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        sedeService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SedeResponseDTO> cambiarEstado(
            @PathVariable("id") Integer id,
            @RequestParam("activo") boolean activo) {
        return ResponseEntity.ok(sedeService.cambiarEstado(id, activo));
    }
}
