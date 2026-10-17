package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.AjusteStockRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.*;
import com.dwi.intranetdistribucion.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventario", "/api/inventario"})
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<InventarioConsultaDTO> consultarInventario(
            @RequestParam(name = "idAlmacen", required = false) Integer idAlmacen,
            @RequestParam(name = "idCategoria", required = false) Integer idCategoria,
            @RequestParam(name = "soloBajoStock", required = false, defaultValue = "false") Boolean soloBajoStock,
            @RequestParam(name = "buscar", required = false) String buscar) {
        return ResponseEntity.ok(inventarioService.consultarInventario(idAlmacen, idCategoria, soloBajoStock, buscar));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaStockDTO>> obtenerAlertas(
            @RequestParam(name = "idAlmacen", required = false) Integer idAlmacen,
            @RequestParam(name = "idCategoria", required = false) Integer idCategoria) {
        return ResponseEntity.ok(inventarioService.obtenerAlertas(idAlmacen, idCategoria));
    }

    @GetMapping("/{idAlmacen}/{idProducto}")
    public ResponseEntity<InventarioItemDTO> obtenerStockProducto(
            @PathVariable("idAlmacen") Integer idAlmacen,
            @PathVariable("idProducto") Integer idProducto) {
        return ResponseEntity.ok(inventarioService.obtenerStockProducto(idAlmacen, idProducto));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoResponseDTO> registrarMovimiento(
            @Valid @RequestBody MovimientoRequestDTO request,
            Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return new ResponseEntity<>(inventarioService.registrarMovimiento(request, username), HttpStatus.CREATED);
    }

    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoResponseDTO> ajustarStock(
            @Valid @RequestBody AjusteStockRequestDTO request,
            Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(inventarioService.ajustarStock(request, username));
    }

    @GetMapping("/tipos-movimiento")
    public ResponseEntity<List<TipoMovimientoResponseDTO>> obtenerTiposMovimiento() {
        return ResponseEntity.ok(inventarioService.obtenerTiposMovimiento());
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoResponseDTO>> obtenerHistorialMovimientos(
            @RequestParam(name = "idAlmacen", required = false) Integer idAlmacen,
            @RequestParam(name = "idProducto", required = false) Integer idProducto,
            @RequestParam(name = "idTipoMovimiento", required = false) Short idTipoMovimiento,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaFin) {
        return ResponseEntity.ok(inventarioService.obtenerHistorialMovimientos(
                idAlmacen,
                idProducto,
                idTipoMovimiento,
                fechaInicio,
                fechaFin
        ));
    }
}
