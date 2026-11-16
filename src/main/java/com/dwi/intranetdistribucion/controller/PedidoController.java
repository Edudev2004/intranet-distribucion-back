package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.PedidoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteOpcionDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoResumenDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoDisponibleDTO;
import com.dwi.intranetdistribucion.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoResumenDTO>> listar(@RequestParam(required = false) Integer idEstado) {
        return ResponseEntity.ok(pedidoService.obtenerTodos(idEstado));
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteOpcionDTO>> clientes() {
        return ResponseEntity.ok(pedidoService.obtenerClientes());
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDisponibleDTO>> productos(@RequestParam Integer idAlmacen) {
        return ResponseEntity.ok(pedidoService.obtenerProductosDisponibles(idAlmacen));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(@Valid @RequestBody PedidoRequestDTO request,
                                                   Principal principal) {
        String email = (principal != null) ? principal.getName() : null;
        return new ResponseEntity<>(pedidoService.crear(request, email), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}