package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.IncidenciaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.IncidenciaResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoIncidenciaResponseDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Entrega;
import com.dwi.intranetdistribucion.model.Incidencia;
import com.dwi.intranetdistribucion.model.TipoIncidencia;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.EntregaRepository;
import com.dwi.intranetdistribucion.repository.IncidenciaRepository;
import com.dwi.intranetdistribucion.repository.TipoIncidenciaRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.IncidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaServiceImpl implements IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final TipoIncidenciaRepository tipoIncidenciaRepository;
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public IncidenciaResponseDTO registrar(IncidenciaRequestDTO request) {
        Entrega entrega = buscarEntrega(request.getIdEntrega());

        TipoIncidencia tipoIncidencia = tipoIncidenciaRepository.findById(request.getIdTipoIncidencia())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de incidencia no encontrado con id: " + request.getIdTipoIncidencia()));

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + request.getIdUsuario()));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || Boolean.TRUE.equals(usuario.getBloqueado())) {
            throw new BadRequestException("El usuario " + usuario.getUsername()
                    + " está inactivo o bloqueado y no puede registrar incidencias");
        }

        Incidencia incidencia = Incidencia.builder()
                .entrega(entrega)
                .tipoIncidencia(tipoIncidencia)
                .comentario(normalizarComentario(request.getComentario()))
                .usuario(usuario)
                .build();

        return mapearADTO(incidenciaRepository.save(incidencia));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> listar(Long idEntrega, Long idPedido) {
        List<Incidencia> incidencias;
        if (idEntrega != null) {
            buscarEntrega(idEntrega);
            incidencias = incidenciaRepository.findByEntregaIdEntregaOrderByFechaHoraDesc(idEntrega);
        } else if (idPedido != null) {
            incidencias = incidenciaRepository.findByEntregaIdPedidoOrderByFechaHoraDesc(idPedido);
        } else {
            incidencias = incidenciaRepository.findAllByOrderByFechaHoraDesc();
        }
        return incidencias.stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenciaResponseDTO obtenerPorId(Long id) {
        return incidenciaRepository.findById(id)
                .map(this::mapearADTO)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoIncidenciaResponseDTO> listarTipos() {
        return tipoIncidenciaRepository.findAll(Sort.by("nombre")).stream()
                .map(tipo -> TipoIncidenciaResponseDTO.builder()
                        .idTipoIncidencia(tipo.getIdTipoIncidencia())
                        .nombre(tipo.getNombre())
                        .build())
                .toList();
    }

    private Entrega buscarEntrega(Long idEntrega) {
        return entregaRepository.findById(idEntrega)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada con id: " + idEntrega));
    }

    private String normalizarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        return comentario.trim();
    }

    private IncidenciaResponseDTO mapearADTO(Incidencia incidencia) {
        Entrega entrega = incidencia.getEntrega();
        TipoIncidencia tipo = incidencia.getTipoIncidencia();
        Usuario usuario = incidencia.getUsuario();
        return IncidenciaResponseDTO.builder()
                .idIncidencia(incidencia.getIdIncidencia())
                .idEntrega(entrega.getIdEntrega())
                .idPedido(entrega.getIdPedido())
                .idProgramacion(entrega.getIdProgramacion())
                .idTipoIncidencia(tipo.getIdTipoIncidencia())
                .tipoIncidencia(tipo.getNombre())
                .comentario(incidencia.getComentario())
                .fechaHora(incidencia.getFechaHora())
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .nombreUsuario(usuario.getNombres() + " " + usuario.getApellidos())
                .build();
    }
}
