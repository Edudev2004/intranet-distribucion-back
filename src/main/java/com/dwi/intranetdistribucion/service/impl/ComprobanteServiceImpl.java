package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ComprobanteRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ComprobantePdfDTO;
import com.dwi.intranetdistribucion.dto.response.ComprobanteResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoComprobanteResponseDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.ClaseComprobante;
import com.dwi.intranetdistribucion.model.Comprobante;
import com.dwi.intranetdistribucion.model.Pedido;
import com.dwi.intranetdistribucion.model.TipoComprobante;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.report.ComprobantePdfGenerator;
import com.dwi.intranetdistribucion.repository.ComprobanteRepository;
import com.dwi.intranetdistribucion.repository.PedidoRepository;
import com.dwi.intranetdistribucion.repository.TipoComprobanteRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.repository.projection.PedidoCabeceraView;
import com.dwi.intranetdistribucion.repository.projection.PedidoDetalleView;
import com.dwi.intranetdistribucion.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final TipoComprobanteRepository tipoComprobanteRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComprobantePdfGenerator comprobantePdfGenerator;

    @Override
    @Transactional
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO request) {
        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + request.getIdPedido()));

        // El bloqueo sobre el tipo serializa emisiones concurrentes y evita números correlativos repetidos
        TipoComprobante tipo = tipoComprobanteRepository.findByIdParaEmitir(request.getIdTipoComprobante())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de comprobante no encontrado con id: " + request.getIdTipoComprobante()));
        ClaseComprobante clase = ClaseComprobante.desde(tipo);

        Usuario usuario = buscarUsuarioHabilitado(request.getIdUsuario());

        List<Comprobante> existentes = comprobanteRepository
                .findByPedidoIdPedidoAndTipoComprobanteIdTipoComprobanteOrderByFechaEmisionDesc(
                        pedido.getIdPedido(), tipo.getIdTipoComprobante());
        if (!existentes.isEmpty()) {
            throw new DuplicateResourceException("El pedido N° " + pedido.getIdPedido() + " ya tiene "
                    + tipo.getNombre() + " emitida: " + existentes.get(0).getNumeroCompleto());
        }

        PedidoCabeceraView cabecera = obtenerCabecera(pedido.getIdPedido());
        if (clase == ClaseComprobante.FACTURA && !esRuc(cabecera.getTipoDocumentoCliente())) {
            throw new BadRequestException("Para emitir una factura el cliente debe estar identificado con RUC "
                    + "(documento actual: " + cabecera.getTipoDocumentoCliente() + "). Emita una boleta.");
        }
        if (pedidoRepository.obtenerDetalle(pedido.getIdPedido()).isEmpty()) {
            throw new BadRequestException("El pedido N° " + pedido.getIdPedido()
                    + " no tiene productos registrados; no se puede emitir el documento");
        }

        String serie = resolverSerie(request.getSerie(), clase, tipo);
        int numero = comprobanteRepository.obtenerUltimoNumero(tipo.getIdTipoComprobante(), serie) + 1;

        Comprobante comprobante = Comprobante.builder()
                .pedido(pedido)
                .tipoComprobante(tipo)
                .serie(serie)
                .numero(numero)
                .usuario(usuario)
                .build();

        return mapearADTO(comprobanteRepository.save(comprobante));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComprobanteResponseDTO> listar(Long idPedido, Short idTipoComprobante) {
        List<Comprobante> comprobantes;
        if (idPedido != null && idTipoComprobante != null) {
            comprobantes = comprobanteRepository
                    .findByPedidoIdPedidoAndTipoComprobanteIdTipoComprobanteOrderByFechaEmisionDesc(idPedido, idTipoComprobante);
        } else if (idPedido != null) {
            comprobantes = comprobanteRepository.findByPedidoIdPedidoOrderByFechaEmisionDesc(idPedido);
        } else if (idTipoComprobante != null) {
            comprobantes = comprobanteRepository.findByTipoComprobanteIdTipoComprobanteOrderByFechaEmisionDesc(idTipoComprobante);
        } else {
            comprobantes = comprobanteRepository.findAllByOrderByFechaEmisionDesc();
        }
        return comprobantes.stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobanteResponseDTO obtenerPorId(Long id) {
        return mapearADTO(buscarComprobante(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobantePdfDTO generarPdf(Long id) {
        Comprobante comprobante = buscarComprobante(id);
        Long idPedido = comprobante.getPedido().getIdPedido();
        PedidoCabeceraView cabecera = obtenerCabecera(idPedido);
        List<PedidoDetalleView> detalle = pedidoRepository.obtenerDetalle(idPedido);

        byte[] contenido = comprobantePdfGenerator.generar(
                comprobante, ClaseComprobante.desde(comprobante.getTipoComprobante()), cabecera, detalle);
        return new ComprobantePdfDTO(comprobante.getNumeroCompleto() + ".pdf", contenido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoComprobanteResponseDTO> listarTipos() {
        return tipoComprobanteRepository.findAll(Sort.by("nombre")).stream()
                .map(tipo -> TipoComprobanteResponseDTO.builder()
                        .idTipoComprobante(tipo.getIdTipoComprobante())
                        .nombre(tipo.getNombre())
                        .build())
                .toList();
    }

    private Comprobante buscarComprobante(Long id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado con id: " + id));
    }

    private PedidoCabeceraView obtenerCabecera(Long idPedido) {
        return pedidoRepository.obtenerCabecera(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontraron los datos de cliente/almacén del pedido con id: " + idPedido));
    }

    private Usuario buscarUsuarioHabilitado(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + idUsuario));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || Boolean.TRUE.equals(usuario.getBloqueado())) {
            throw new BadRequestException("El usuario " + usuario.getUsername()
                    + " está inactivo o bloqueado y no puede emitir comprobantes");
        }
        return usuario;
    }

    private String resolverSerie(String serieSolicitada, ClaseComprobante clase, TipoComprobante tipo) {
        String seriePorDefecto = clase.seriePorDefecto(tipo);
        if (serieSolicitada == null || serieSolicitada.isBlank()) {
            return seriePorDefecto;
        }
        String serie = serieSolicitada.trim().toUpperCase(Locale.ROOT);
        if (clase != ClaseComprobante.OTRO && serie.charAt(0) != seriePorDefecto.charAt(0)) {
            throw new BadRequestException("La serie de " + tipo.getNombre() + " debe empezar con '"
                    + seriePorDefecto.charAt(0) + "' (ej. " + seriePorDefecto + ")");
        }
        return serie;
    }

    private boolean esRuc(String tipoDocumento) {
        return tipoDocumento != null && tipoDocumento.toUpperCase(Locale.ROOT).contains("RUC");
    }

    private ComprobanteResponseDTO mapearADTO(Comprobante comprobante) {
        TipoComprobante tipo = comprobante.getTipoComprobante();
        Usuario usuario = comprobante.getUsuario();
        return ComprobanteResponseDTO.builder()
                .idComprobante(comprobante.getIdComprobante())
                .idPedido(comprobante.getPedido().getIdPedido())
                .idTipoComprobante(tipo.getIdTipoComprobante())
                .tipoComprobante(tipo.getNombre())
                .serie(comprobante.getSerie())
                .numero(comprobante.getNumero())
                .numeroCompleto(comprobante.getNumeroCompleto())
                .fechaEmision(comprobante.getFechaEmision())
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .nombreUsuario(usuario.getNombres() + " " + usuario.getApellidos())
                .build();
    }
}
