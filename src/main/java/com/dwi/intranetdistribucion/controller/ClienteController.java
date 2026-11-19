package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.ClienteRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteOpcionDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoDocumentoDTO;
import com.dwi.intranetdistribucion.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false, defaultValue = "true") Boolean soloActivos) {
        return ResponseEntity.ok(clienteService.listarTodos(filtro, soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam Boolean activo) {
        clienteService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/opciones")
    public ResponseEntity<List<ClienteOpcionDTO>> listarOpciones() {
        return ResponseEntity.ok(clienteService.listarOpciones());
    }

    @GetMapping("/tipos-documento")
    public ResponseEntity<List<TipoDocumentoDTO>> listarTiposDocumento() {
        return ResponseEntity.ok(clienteService.listarTiposDocumento());
    }
}
