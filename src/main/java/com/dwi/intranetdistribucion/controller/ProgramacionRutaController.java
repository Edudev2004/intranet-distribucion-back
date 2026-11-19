package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.AsignarPedidosRequestDTO;
import com.dwi.intranetdistribucion.dto.request.ProgramacionRutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AsignacionPedidosResponseDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoDisponibleAsignacionDTO;
import com.dwi.intranetdistribucion.dto.response.ProgramacionRutaResponseDTO;
import com.dwi.intranetdistribucion.dto.response.ResumenCargaRutaDTO;
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
            @PathVariable("id") Integer id,
            @RequestParam("idEstado") Short idEstado) {
        return ResponseEntity.ok(programacionRutaService.actualizarEstado(id, idEstado));
    }

    // =========================================================================
    // RF-15: ASIGNACIÓN DE PEDIDOS A RUTAS Y CONTROL DE CAPACIDAD DE VEHÍCULO
    // =========================================================================

    @GetMapping("/pedidos-disponibles")
    public ResponseEntity<List<PedidoDisponibleAsignacionDTO>> listarPedidosDisponibles(
            @RequestParam(required = false) Integer idAlmacen,
            @RequestParam(required = false) Integer idZona) {
        return ResponseEntity.ok(programacionRutaService.listarPedidosDisponibles(idAlmacen, idZona));
    }

    @GetMapping("/{id}/resumen-carga")
    public ResponseEntity<ResumenCargaRutaDTO> obtenerResumenCarga(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(programacionRutaService.obtenerResumenCarga(id));
    }

    @PostMapping("/{id}/asignar-pedidos")
    public ResponseEntity<AsignacionPedidosResponseDTO> asignarPedidos(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AsignarPedidosRequestDTO request) {
        return ResponseEntity.ok(programacionRutaService.asignarPedidosARuta(id, request));
    }

    @PostMapping("/{id}/auto-asignar-pedidos")
    public ResponseEntity<AsignacionPedidosResponseDTO> autoAsignarPedidos(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(programacionRutaService.autoAsignarPedidosARuta(id));
    }

    @DeleteMapping("/{id}/pedidos/{idPedido}")
    public ResponseEntity<Void> desasignarPedido(
            @PathVariable("id") Integer id,
            @PathVariable("idPedido") Long idPedido) {
        programacionRutaService.desasignarPedidoDeRuta(id, idPedido);
        return ResponseEntity.noContent().build();
    }
}
