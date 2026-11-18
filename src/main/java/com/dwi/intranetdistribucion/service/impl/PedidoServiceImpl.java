package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.AprobacionPedidoRequestDTO;
import com.dwi.intranetdistribucion.dto.request.DetallePedidoRequestDTO;
import com.dwi.intranetdistribucion.dto.request.PedidoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AprobacionPedidoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteOpcionDTO;
import com.dwi.intranetdistribucion.dto.response.DetallePedidoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoResumenDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoDisponibleDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Almacen;
import com.dwi.intranetdistribucion.model.AprobacionPedido;
import com.dwi.intranetdistribucion.model.DetallePedido;
import com.dwi.intranetdistribucion.model.Inventario;
import com.dwi.intranetdistribucion.model.Pedido;
import com.dwi.intranetdistribucion.model.Producto;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.AlmacenRepository;
import com.dwi.intranetdistribucion.repository.AprobacionPedidoRepository;
import com.dwi.intranetdistribucion.repository.DetallePedidoRepository;
import com.dwi.intranetdistribucion.repository.InventarioRepository;
import com.dwi.intranetdistribucion.repository.PedidoRepository;
import com.dwi.intranetdistribucion.repository.ProductoRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private static final Short ESTADO_PENDIENTE = 1;

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final AlmacenRepository almacenRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AprobacionPedidoRepository aprobacionPedidoRepository; // Añadido para RF-11

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenDTO> obtenerTodos(Integer idEstado) {
        return pedidoRepository.listarResumen(idEstado).stream()
                .map(v -> PedidoResumenDTO.builder()
                        .idPedido(v.getIdPedido())
                        .cliente(v.getCliente())
                        .almacen(v.getAlmacen())
                        .idEstadoPedido(v.getIdEstadoPedido())
                        .estado(v.getEstado())
                        .fechaPedido(v.getFechaPedido())
                        .total(v.getTotal())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = buscarPedido(id);
        return armarRespuesta(pedido, detallePedidoRepository.findByIdPedido(id));
    }

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO request, String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new BadRequestException("No se pudo identificar al usuario autenticado");
        }
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        pedidoRepository.obtenerNombreClienteActivo(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado o inactivo con id: " + request.getIdCliente()));

        Almacen almacen = almacenRepository.findById(request.getIdAlmacen())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Almacén no encontrado con id: " + request.getIdAlmacen()));
        if (!Boolean.TRUE.equals(almacen.getActivo())) {
            throw new BadRequestException("El almacén '" + almacen.getNombre() + "' está inactivo");
        }

        // Si un producto se repite, se suman sus cantidades
        Map<Integer, BigDecimal> cantidades = new LinkedHashMap<>();
        for (DetallePedidoRequestDTO d : request.getDetalles()) {
            cantidades.merge(d.getIdProducto(), d.getCantidad(), BigDecimal::add);
        }

        // Criterio: validar la disponibilidad de stock antes de confirmar
        Map<Integer, Producto> productos = new LinkedHashMap<>();
        List<String> faltantes = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> linea : cantidades.entrySet()) {
            Producto producto = productoRepository.findById(linea.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: " + linea.getKey()));
            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new BadRequestException("El producto '" + producto.getNombre() + "' está inactivo");
            }
            BigDecimal stock = inventarioRepository
                    .findByIdAlmacenAndIdProducto(almacen.getIdAlmacen(), producto.getIdProducto())
                    .map(Inventario::getStockActual)
                    .orElse(BigDecimal.ZERO);
            if (stock.compareTo(linea.getValue()) < 0) {
                faltantes.add(producto.getCodigo() + " (" + producto.getNombre() + "): disponible "
                        + formatear(stock) + ", solicitado " + formatear(linea.getValue()));
            }
            productos.put(producto.getIdProducto(), producto);
        }
        if (!faltantes.isEmpty()) {
            throw new BadRequestException("Stock insuficiente en " + almacen.getNombre() + " para: "
                    + String.join("; ", faltantes));
        }

        // Criterio: el pedido queda en estado "Pendiente" al crearse
        Pedido pedido = pedidoRepository.save(Pedido.builder()
                .idCliente(request.getIdCliente())
                .idAlmacen(almacen.getIdAlmacen())
                .idUsuarioRegistra(usuario.getIdUsuario())
                .idEstadoPedido(ESTADO_PENDIENTE)
                .fechaPedido(OffsetDateTime.now())
                .fechaEntregaSolicitada(request.getFechaEntregaSolicitada())
                .observacion(normalizar(request.getObservacion()))
                .build());

        // El precio se toma del producto, no del cliente web
        List<DetallePedido> detalles = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> linea : cantidades.entrySet()) {
            Producto producto = productos.get(linea.getKey());
            detalles.add(DetallePedido.builder()
                    .idPedido(pedido.getIdPedido())
                    .idProducto(producto.getIdProducto())
                    .cantidad(linea.getValue())
                    .precioUnitario(producto.getPrecioUnitario())
                    .build());
        }
        detallePedidoRepository.saveAll(detalles);

        return armarRespuesta(pedido, detalles);
    }

    @Override
    @Transactional
    public AprobacionPedidoResponseDTO aprobarRechazarPedido(Long idPedido, AprobacionPedidoRequestDTO request, String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new BadRequestException("No se pudo identificar al usuario autenticado");
        }
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        Pedido pedido = buscarPedido(idPedido);

        if (!ESTADO_PENDIENTE.equals(pedido.getIdEstadoPedido())) {
            throw new BadRequestException("El pedido ya ha sido procesado anteriormente o no se encuentra en estado Pendiente.");
        }

        if (aprobacionPedidoRepository.existsByPedidoIdPedido(idPedido)) {
            throw new BadRequestException("Ya existe una decisión registrada para este pedido.");
        }

        boolean esRechazado = "RECHAZADO".equalsIgnoreCase(request.getDecision());
        if (esRechazado && (request.getMotivo() == null || request.getMotivo().isBlank())) {
            throw new BadRequestException("Debe especificar un motivo para el rechazo del pedido.");
        }

        // Definir nuevo ID de estado (2 = Aprobado, 3 = Rechazado según catálogos habituales)
        Short nuevoEstadoId = esRechazado ? (short) 3 : (short) 2;
        pedido.setIdEstadoPedido(nuevoEstadoId);
        pedidoRepository.save(pedido);

        AprobacionPedido aprobacion = aprobacionPedidoRepository.save(AprobacionPedido.builder()
                .pedido(pedido)
                .usuario(usuario)
                .decision(request.getDecision().toUpperCase())
                .motivo(normalizar(request.getMotivo()))
                .fecha(OffsetDateTime.now())
                .build());

        return AprobacionPedidoResponseDTO.builder()
                .idAprobacion(aprobacion.getIdAprobacion())
                .idPedido(pedido.getIdPedido())
                .decision(aprobacion.getDecision())
                .motivo(aprobacion.getMotivo())
                .usuarioAprobador(usuario.getNombres() + " " + usuario.getApellidos())
                .fecha(aprobacion.getFecha())
                .build();
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Pedido pedido = buscarPedido(id);
        if (!ESTADO_PENDIENTE.equals(pedido.getIdEstadoPedido())) {
            throw new BadRequestException("Solo se pueden eliminar pedidos en estado Pendiente");
        }
        detallePedidoRepository.deleteByIdPedido(id);
        pedidoRepository.delete(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteOpcionDTO> obtenerClientes() {
        return pedidoRepository.listarClientes().stream()
                .map(v -> ClienteOpcionDTO.builder()
                        .idCliente(v.getIdCliente())
                        .nombre(v.getNombre())
                        .documento(v.getDocumento())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDisponibleDTO> obtenerProductosDisponibles(Integer idAlmacen) {
        return pedidoRepository.listarProductosDisponibles(idAlmacen).stream()
                .map(v -> ProductoDisponibleDTO.builder()
                        .idProducto(v.getIdProducto())
                        .codigo(v.getCodigo())
                        .producto(v.getProducto())
                        .precioUnitario(v.getPrecioUnitario())
                        .stockActual(v.getStockActual())
                        .build())
                .toList();
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
    }

    // Criterio: el total se calcula automáticamente
    private PedidoResponseDTO armarRespuesta(Pedido pedido, List<DetallePedido> detalles) {
        Map<Integer, Producto> productos = productoRepository
                .findAllById(detalles.stream().map(DetallePedido::getIdProducto).toList())
                .stream()
                .collect(Collectors.toMap(Producto::getIdProducto, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedidoResponseDTO> lineas = new ArrayList<>();
        for (DetallePedido d : detalles) {
            Producto producto = productos.get(d.getIdProducto());
            BigDecimal subtotal = d.getCantidad().multiply(d.getPrecioUnitario());
            total = total.add(subtotal);
            lineas.add(DetallePedidoResponseDTO.builder()
                    .idProducto(d.getIdProducto())
                    .codigo(producto != null ? producto.getCodigo() : "-")
                    .producto(producto != null ? producto.getNombre() : "-")
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build());
        }

        return PedidoResponseDTO.builder()
                .idPedido(pedido.getIdPedido())
                .idCliente(pedido.getIdCliente())
                .cliente(pedidoRepository.obtenerNombreCliente(pedido.getIdCliente()).orElse("-"))
                .idAlmacen(pedido.getIdAlmacen())
                .almacen(almacenRepository.findById(pedido.getIdAlmacen()).map(Almacen::getNombre).orElse("-"))
                .idEstadoPedido(pedido.getIdEstadoPedido().intValue())
                .estado(pedidoRepository.obtenerNombreEstado(pedido.getIdEstadoPedido().intValue()).orElse("-"))
                .fechaPedido(pedido.getFechaPedido())
                .fechaEntregaSolicitada(pedido.getFechaEntregaSolicitada())
                .observacion(pedido.getObservacion())
                .total(total)
                .detalles(lineas)
                .build();
    }

    private String formatear(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}