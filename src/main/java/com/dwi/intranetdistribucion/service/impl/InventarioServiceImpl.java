package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.AjusteStockRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoItemRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.*;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.*;
import com.dwi.intranetdistribucion.repository.*;
import com.dwi.intranetdistribucion.repository.projection.InventarioDetalleView;
import com.dwi.intranetdistribucion.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final AlmacenRepository almacenRepository;
    private final ProductoRepository productoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public InventarioConsultaDTO consultarInventario(Integer idAlmacen, Integer idCategoria, Boolean soloBajoStock, String buscar) {
        boolean filtrarBajoStock = Boolean.TRUE.equals(soloBajoStock);
        String terminoBusqueda = (buscar != null && !buscar.isBlank()) ? buscar.trim() : null;

        List<InventarioDetalleView> views = inventarioRepository.consultarInventario(
                idAlmacen,
                idCategoria,
                filtrarBajoStock,
                terminoBusqueda
        );

        List<InventarioItemDTO> items = views.stream()
                .map(this::mapearItemDTO)
                .toList();

        int totalProductos = items.size();
        int totalBajoStock = (int) items.stream().filter(InventarioItemDTO::getBajoStock).count();
        int totalSinStock = (int) items.stream()
                .filter(i -> i.getStockActual() != null && i.getStockActual().compareTo(BigDecimal.ZERO) <= 0)
                .count();

        String nombreAlmacen = (idAlmacen != null) ? inventarioRepository.obtenerNombreAlmacen(idAlmacen).orElse(null) : null;

        return InventarioConsultaDTO.builder()
                .items(items)
                .totalProductos(totalProductos)
                .totalBajoStock(totalBajoStock)
                .totalSinStock(totalSinStock)
                .almacenFiltro(nombreAlmacen)
                .categoriaFiltro(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaStockDTO> obtenerAlertas(Integer idAlmacen, Integer idCategoria) {
        List<InventarioDetalleView> views = inventarioRepository.consultarAlertas(idAlmacen, idCategoria);

        return views.stream().map(v -> {
            BigDecimal actual = v.getStockActual() != null ? v.getStockActual() : BigDecimal.ZERO;
            BigDecimal minimo = v.getStockMinimo() != null ? v.getStockMinimo() : BigDecimal.ZERO;
            BigDecimal diff = actual.subtract(minimo);
            String nivel = actual.compareTo(BigDecimal.ZERO) <= 0 ? "SIN_STOCK" : "BAJO_STOCK";

            String mensaje = (actual.compareTo(BigDecimal.ZERO) <= 0)
                    ? "¡Crítico! El producto '" + v.getProducto() + "' (" + v.getCodigo() + ") se encuentra sin stock en " + v.getAlmacen()
                    : "Alerta: El producto '" + v.getProducto() + "' (" + v.getCodigo() + ") tiene stock actual de "
                    + actual + " " + v.getUnidad() + ", igual o inferior al mínimo configurado (" + minimo + ") en " + v.getAlmacen();

            return AlertaStockDTO.builder()
                    .idAlmacen(v.getIdAlmacen())
                    .almacen(v.getAlmacen())
                    .sede(v.getSede())
                    .idProducto(v.getIdProducto())
                    .codigo(v.getCodigo())
                    .producto(v.getProducto())
                    .categoria(v.getCategoria())
                    .unidad(v.getUnidad())
                    .stockActual(actual)
                    .stockMinimo(minimo)
                    .diferencia(diff)
                    .nivelAlerta(nivel)
                    .mensaje(mensaje)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioItemDTO obtenerStockProducto(Integer idAlmacen, Integer idProducto) {
        Almacen almacen = buscarAlmacen(idAlmacen);
        Producto producto = buscarProducto(idProducto);

        Inventario inventario = inventarioRepository.findByIdAlmacenAndIdProducto(idAlmacen, idProducto)
                .orElse(null);

        BigDecimal stockActual = (inventario != null && inventario.getStockActual() != null)
                ? inventario.getStockActual()
                : BigDecimal.ZERO;
        BigDecimal stockMinimo = (producto.getStockMinimo() != null)
                ? producto.getStockMinimo()
                : BigDecimal.ZERO;

        boolean bajoStock = stockActual.compareTo(stockMinimo) <= 0;
        String nivelAlerta = (stockActual.compareTo(BigDecimal.ZERO) <= 0)
                ? "SIN_STOCK"
                : (bajoStock ? "BAJO_STOCK" : "NORMAL");

        String mensaje = bajoStock
                ? "Alerta: El stock actual (" + stockActual + ") ha alcanzado o está por debajo del mínimo (" + stockMinimo + ") en " + almacen.getNombre()
                : null;

        return InventarioItemDTO.builder()
                .idAlmacen(almacen.getIdAlmacen())
                .almacen(almacen.getNombre())
                .sede(almacen.getSede() != null ? almacen.getSede().getNombre() : null)
                .idProducto(producto.getIdProducto())
                .codigo(producto.getCodigo())
                .producto(producto.getNombre())
                .categoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                .unidad(producto.getUnidadMedida() != null ? producto.getUnidadMedida().getCodigo() : null)
                .stockActual(stockActual)
                .stockMinimo(stockMinimo)
                .precioUnitario(producto.getPrecioUnitario())
                .bajoStock(bajoStock)
                .nivelAlerta(nivelAlerta)
                .mensajeAlerta(mensaje)
                .build();
    }

    @Override
    @Transactional
    public MovimientoResponseDTO registrarMovimiento(MovimientoRequestDTO request, String username) {
        Almacen almacen = buscarAlmacen(request.getIdAlmacen());
        TipoMovimiento tipo = buscarTipoMovimiento(request.getIdTipoMovimiento());
        Usuario usuario = resolverUsuario(username);
        Pedido pedido = (request.getIdPedido() != null)
                ? pedidoRepository.findById(request.getIdPedido()).orElse(null)
                : null;

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .almacen(almacen)
                .tipoMovimiento(tipo)
                .usuario(usuario)
                .pedido(pedido)
                .fecha(OffsetDateTime.now())
                .observacion(request.getObservacion())
                .build();

        movimiento = movimientoInventarioRepository.save(movimiento);

        boolean contieneAlertas = false;
        List<MovimientoDetalleResponseDTO> detallesDTO = new ArrayList<>();

        for (MovimientoItemRequestDTO itemReq : request.getItems()) {
            Producto producto = buscarProducto(itemReq.getIdProducto());
            BigDecimal cantidad = itemReq.getCantidad();

            if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("La cantidad debe ser mayor a cero para el producto: " + producto.getNombre());
            }

            Inventario inventario = inventarioRepository.findByIdAlmacenAndIdProducto(almacen.getIdAlmacen(), producto.getIdProducto())
                    .orElseGet(() -> Inventario.builder()
                            .idAlmacen(almacen.getIdAlmacen())
                            .idProducto(producto.getIdProducto())
                            .stockActual(BigDecimal.ZERO)
                            .build());

            BigDecimal stockActual = inventario.getStockActual() != null ? inventario.getStockActual() : BigDecimal.ZERO;
            Character naturaleza = tipo.getNaturaleza();

            if (naturaleza == 'E') {
                inventario.aumentarStock(cantidad);
            } else if (naturaleza == 'S') {
                if (stockActual.compareTo(cantidad) < 0) {
                    throw new BadRequestException("Stock insuficiente en el almacén '" + almacen.getNombre()
                            + "' para el producto '" + producto.getNombre() + "' (" + producto.getCodigo()
                            + "). Stock disponible: " + stockActual + ", cantidad requerida: " + cantidad);
                }
                inventario.disminuirStock(cantidad);
            } else {
                throw new BadRequestException("Naturaleza de movimiento desconocida: " + naturaleza);
            }

            Inventario actualizado = inventarioRepository.save(inventario);

            BigDecimal nuevoStock = actualizado.getStockActual();
            BigDecimal stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : BigDecimal.ZERO;
            boolean bajoStock = nuevoStock.compareTo(stockMinimo) <= 0;

            String mensajeAlerta = null;
            if (bajoStock) {
                contieneAlertas = true;
                mensajeAlerta = (nuevoStock.compareTo(BigDecimal.ZERO) <= 0)
                        ? "¡Alerta Crítica! El producto '" + producto.getNombre() + "' se ha quedado sin stock."
                        : "Alerta: El stock del producto '" + producto.getNombre() + "' (" + nuevoStock + ") ha alcanzado o está por debajo del mínimo (" + stockMinimo + ").";
            }

            DetalleMovimiento detalle = DetalleMovimiento.builder()
                    .idMovimiento(movimiento.getIdMovimiento())
                    .idProducto(producto.getIdProducto())
                    .cantidad(cantidad)
                    .build();
            movimiento.agregarDetalle(detalle);

            detallesDTO.add(MovimientoDetalleResponseDTO.builder()
                    .idProducto(producto.getIdProducto())
                    .codigo(producto.getCodigo())
                    .producto(producto.getNombre())
                    .cantidad(cantidad)
                    .stockResultante(nuevoStock)
                    .alertaStock(bajoStock)
                    .mensajeAlerta(mensajeAlerta)
                    .build());
        }

        return MovimientoResponseDTO.builder()
                .idMovimiento(movimiento.getIdMovimiento())
                .idAlmacen(almacen.getIdAlmacen())
                .almacen(almacen.getNombre())
                .idTipoMovimiento(tipo.getIdTipoMovimiento())
                .tipoMovimiento(tipo.getNombre())
                .naturaleza(tipo.getNaturaleza())
                .idUsuario(usuario.getIdUsuario())
                .usuario(usuario.getNombres() + " " + usuario.getApellidos())
                .idPedido(pedido != null ? pedido.getIdPedido() : null)
                .fecha(movimiento.getFecha())
                .observacion(movimiento.getObservacion())
                .items(detallesDTO)
                .contieneAlertas(contieneAlertas)
                .build();
    }

    @Override
    @Transactional
    public MovimientoResponseDTO ajustarStock(AjusteStockRequestDTO request, String username) {
        Almacen almacen = buscarAlmacen(request.getIdAlmacen());
        Producto producto = buscarProducto(request.getIdProducto());
        BigDecimal nuevoStock = request.getNuevoStock();

        if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El nuevo stock no puede ser negativo");
        }

        Inventario inventario = inventarioRepository.findByIdAlmacenAndIdProducto(almacen.getIdAlmacen(), producto.getIdProducto())
                .orElseGet(() -> Inventario.builder()
                        .idAlmacen(almacen.getIdAlmacen())
                        .idProducto(producto.getIdProducto())
                        .stockActual(BigDecimal.ZERO)
                        .build());

        BigDecimal stockActual = inventario.getStockActual() != null ? inventario.getStockActual() : BigDecimal.ZERO;
        int comparacion = nuevoStock.compareTo(stockActual);

        if (comparacion == 0) {
            return MovimientoResponseDTO.builder()
                    .idMovimiento(null)
                    .idAlmacen(almacen.getIdAlmacen())
                    .almacen(almacen.getNombre())
                    .observacion("El stock ya coincide con el valor indicado (" + nuevoStock + "). Sin cambios realizados.")
                    .items(List.of(MovimientoDetalleResponseDTO.builder()
                            .idProducto(producto.getIdProducto())
                            .codigo(producto.getCodigo())
                            .producto(producto.getNombre())
                            .cantidad(BigDecimal.ZERO)
                            .stockResultante(stockActual)
                            .alertaStock(stockActual.compareTo(producto.getStockMinimo()) <= 0)
                            .build()))
                    .contieneAlertas(stockActual.compareTo(producto.getStockMinimo()) <= 0)
                    .build();
        }

        TipoMovimiento tipo;
        BigDecimal cantidad;
        if (comparacion > 0) {
            cantidad = nuevoStock.subtract(stockActual);
            tipo = tipoMovimientoRepository.findByNombreIgnoreCase("ENTRADA_AJUSTE")
                    .orElseGet(() -> tipoMovimientoRepository.findByNaturaleza('E').stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No se encontró tipo de movimiento de entrada")));
        } else {
            cantidad = stockActual.subtract(nuevoStock);
            tipo = tipoMovimientoRepository.findByNombreIgnoreCase("SALIDA_AJUSTE")
                    .orElseGet(() -> tipoMovimientoRepository.findByNaturaleza('S').stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No se encontró tipo de movimiento de salida")));
        }

        String obs = (request.getMotivo() != null && !request.getMotivo().isBlank())
                ? request.getMotivo().trim()
                : "Ajuste manual de stock de " + stockActual + " a " + nuevoStock;

        MovimientoRequestDTO movReq = MovimientoRequestDTO.builder()
                .idAlmacen(almacen.getIdAlmacen())
                .idTipoMovimiento(tipo.getIdTipoMovimiento())
                .observacion(obs)
                .items(List.of(MovimientoItemRequestDTO.builder()
                        .idProducto(producto.getIdProducto())
                        .cantidad(cantidad)
                        .build()))
                .build();

        return registrarMovimiento(movReq, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMovimientoResponseDTO> obtenerTiposMovimiento() {
        return tipoMovimientoRepository.findAll().stream()
                .map(t -> TipoMovimientoResponseDTO.builder()
                        .idTipoMovimiento(t.getIdTipoMovimiento())
                        .nombre(t.getNombre())
                        .naturaleza(t.getNaturaleza())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> obtenerHistorialMovimientos(Integer idAlmacen,
                                                                 Integer idProducto,
                                                                 Short idTipoMovimiento,
                                                                 OffsetDateTime fechaInicio,
                                                                 OffsetDateTime fechaFin) {
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.buscarConFiltros(
                idAlmacen,
                idProducto,
                idTipoMovimiento,
                fechaInicio,
                fechaFin
        );

        return movimientos.stream().map(m -> {
            List<MovimientoDetalleResponseDTO> detalles = m.getDetalles().stream().map(d ->
                    MovimientoDetalleResponseDTO.builder()
                            .idProducto(d.getIdProducto())
                            .codigo(d.getProducto() != null ? d.getProducto().getCodigo() : null)
                            .producto(d.getProducto() != null ? d.getProducto().getNombre() : null)
                            .cantidad(d.getCantidad())
                            .build()
            ).toList();

            return MovimientoResponseDTO.builder()
                    .idMovimiento(m.getIdMovimiento())
                    .idAlmacen(m.getAlmacen().getIdAlmacen())
                    .almacen(m.getAlmacen().getNombre())
                    .idTipoMovimiento(m.getTipoMovimiento().getIdTipoMovimiento())
                    .tipoMovimiento(m.getTipoMovimiento().getNombre())
                    .naturaleza(m.getTipoMovimiento().getNaturaleza())
                    .idUsuario(m.getUsuario().getIdUsuario())
                    .usuario(m.getUsuario().getNombres() + " " + m.getUsuario().getApellidos())
                    .idPedido(m.getPedido() != null ? m.getPedido().getIdPedido() : null)
                    .fecha(m.getFecha())
                    .observacion(m.getObservacion())
                    .items(detalles)
                    .contieneAlertas(false)
                    .build();
        }).toList();
    }

    private InventarioItemDTO mapearItemDTO(InventarioDetalleView v) {
        BigDecimal actual = v.getStockActual() != null ? v.getStockActual() : BigDecimal.ZERO;
        BigDecimal minimo = v.getStockMinimo() != null ? v.getStockMinimo() : BigDecimal.ZERO;
        boolean bajo = actual.compareTo(minimo) <= 0;

        String nivel;
        String mensaje = null;
        if (actual.compareTo(BigDecimal.ZERO) <= 0) {
            nivel = "SIN_STOCK";
            mensaje = "¡Alerta Crítica! Producto sin existencias en almacén " + v.getAlmacen();
        } else if (bajo) {
            nivel = "BAJO_STOCK";
            mensaje = "Alerta: Stock actual (" + actual + ") igual o menor al mínimo requerido (" + minimo + ") en almacén " + v.getAlmacen();
        } else {
            nivel = "NORMAL";
        }

        return InventarioItemDTO.builder()
                .idAlmacen(v.getIdAlmacen())
                .almacen(v.getAlmacen())
                .sede(v.getSede())
                .idProducto(v.getIdProducto())
                .codigo(v.getCodigo())
                .producto(v.getProducto())
                .categoria(v.getCategoria())
                .unidad(v.getUnidad())
                .stockActual(actual)
                .stockMinimo(minimo)
                .precioUnitario(v.getPrecioUnitario())
                .bajoStock(bajo)
                .nivelAlerta(nivel)
                .mensajeAlerta(mensaje)
                .build();
    }

    private Almacen buscarAlmacen(Integer id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con id: " + id));
    }

    private Producto buscarProducto(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    private TipoMovimiento buscarTipoMovimiento(Short id) {
        return tipoMovimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de movimiento no encontrado con id: " + id));
    }

    private Usuario resolverUsuario(String username) {
        if (username != null && !username.isBlank()) {
            return usuarioRepository.findByUsernameIgnoreCase(username)
                    .or(() -> usuarioRepository.findByCorreoIgnoreCase(username))
                    .orElseGet(this::obtenerUsuarioPorDefecto);
        }
        return obtenerUsuarioPorDefecto();
    }

    private Usuario obtenerUsuarioPorDefecto() {
        return usuarioRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún usuario para registrar el movimiento"));
    }
}
