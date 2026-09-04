package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.RolRequestDTO;
import com.dwi.intranetdistribucion.dto.response.PermisoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.RolResponseDTO;
import com.dwi.intranetdistribucion.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(rolService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<RolResponseDTO> crear(@RequestBody RolRequestDTO request) {
        return new ResponseEntity<>(rolService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody RolRequestDTO request) {
        return ResponseEntity.ok(rolService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permisos")
    public ResponseEntity<List<PermisoResponseDTO>> obtenerTodosLosPermisos() {
        return ResponseEntity.ok(rolService.obtenerTodosLosPermisos());
    }
}
