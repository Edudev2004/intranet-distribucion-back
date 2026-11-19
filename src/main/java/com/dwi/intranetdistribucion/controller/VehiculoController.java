package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.VehiculoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EstadoVehiculoDTO;
import com.dwi.intranetdistribucion.dto.response.VehiculoResponseDTO;
import com.dwi.intranetdistribucion.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public ResponseEntity<List<VehiculoResponseDTO>> listar(
            @RequestParam(name = "idEstado", required = false) Short idEstado) {
        return ResponseEntity.ok(vehiculoService.listar(idEstado));
    }

    @GetMapping("/estados")
    public ResponseEntity<List<EstadoVehiculoDTO>> listarEstados() {
        return ResponseEntity.ok(vehiculoService.listarEstados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> obtenerPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> crear(@Valid @RequestBody VehiculoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody VehiculoRequestDTO request) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable("id") Integer id,
            @RequestParam("idEstado") Short idEstado) {
        vehiculoService.cambiarEstado(id, idEstado);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
