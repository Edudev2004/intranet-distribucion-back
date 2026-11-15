package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.MantenimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.MantenimientoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoMantenimientoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.VehiculoOpcionDTO;
import com.dwi.intranetdistribucion.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mantenimientos")
@RequiredArgsConstructor
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    @GetMapping
    public ResponseEntity<List<MantenimientoResponseDTO>> obtenerTodos(
            @RequestParam(required = false) Integer idVehiculo) {
        return ResponseEntity.ok(mantenimientoService.obtenerTodos(idVehiculo));
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoMantenimientoResponseDTO>> obtenerTipos() {
        return ResponseEntity.ok(mantenimientoService.obtenerTipos());
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<List<VehiculoOpcionDTO>> obtenerVehiculos() {
        return ResponseEntity.ok(mantenimientoService.obtenerVehiculos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MantenimientoResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(mantenimientoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<MantenimientoResponseDTO> crear(@Valid @RequestBody MantenimientoRequestDTO request) {
        return new ResponseEntity<>(mantenimientoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MantenimientoResponseDTO> actualizar(@PathVariable Integer id,
                                                               @Valid @RequestBody MantenimientoRequestDTO request) {
        return ResponseEntity.ok(mantenimientoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        mantenimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}