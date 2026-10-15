package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.AlmacenRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AlmacenResponseDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoAlmacenResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoAlmacenResponseDTO;
import com.dwi.intranetdistribucion.service.AlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    @GetMapping
    public ResponseEntity<List<AlmacenResponseDTO>> obtenerTodos(@RequestParam(required = false) Integer idSede) {
        return ResponseEntity.ok(almacenService.obtenerTodos(idSede));
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoAlmacenResponseDTO>> obtenerTipos() {
        return ResponseEntity.ok(almacenService.obtenerTipos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlmacenResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(almacenService.obtenerPorId(id));
    }

    @GetMapping("/{id}/productos")
    public ResponseEntity<List<ProductoAlmacenResponseDTO>> obtenerProductos(@PathVariable Integer id) {
        return ResponseEntity.ok(almacenService.obtenerProductos(id));
    }

    @PostMapping
    public ResponseEntity<AlmacenResponseDTO> crear(@Valid @RequestBody AlmacenRequestDTO request) {
        return new ResponseEntity<>(almacenService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlmacenResponseDTO> actualizar(@PathVariable Integer id,
                                                         @Valid @RequestBody AlmacenRequestDTO request) {
        return ResponseEntity.ok(almacenService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        almacenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}