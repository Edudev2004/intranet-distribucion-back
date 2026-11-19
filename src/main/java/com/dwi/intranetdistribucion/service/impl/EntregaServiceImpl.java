package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ActualizarEntregaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EntregaResponseDTO;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Entrega;
import com.dwi.intranetdistribucion.repository.EntregaRepository;
import com.dwi.intranetdistribucion.repository.PedidoRepository;
import com.dwi.intranetdistribucion.service.EntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarPorProgramacion(Integer idProgramacion) {
        return entregaRepository.findByIdProgramacion(idProgramacion).stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public EntregaResponseDTO actualizarEstadoEntrega(Long idEntrega, ActualizarEntregaRequestDTO request) {
        Entrega entrega = entregaRepository.findById(idEntrega)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada con id: " + idEntrega));

        entrega.setIdEstadoEntrega(request.getIdEstadoEntrega());

        if (request.getFotoEntregaUrl() != null) {
            entrega.setFotoEntregaUrl(request.getFotoEntregaUrl());
        }
        if (request.getFotoGuiaUrl() != null) {
            entrega.setFotoGuiaUrl(request.getFotoGuiaUrl());
        }
        if (request.getNotas() != null) {
            entrega.setNotas(request.getNotas());
        }

        // Si el estado es Entregado (id = 3 o 2 según convención) y horaEntregaReal es null
        if ((request.getIdEstadoEntrega().equals((short) 2) || request.getIdEstadoEntrega().equals((short) 3))
                && entrega.getHoraEntregaReal() == null) {
            entrega.setHoraEntregaReal(OffsetDateTime.now());
        }

        entrega = entregaRepository.save(entrega);
        return mapearRespuesta(entrega);
    }

    private EntregaResponseDTO mapearRespuesta(Entrega entrega) {
        String nombreEstado = entregaRepository.obtenerNombreEstadoEntrega(entrega.getIdEstadoEntrega().intValue())
                .orElse("Desconocido");

        String cliente = null;
        String direccion = null;
        String telefono = null;

        if (entrega.getIdPedido() != null) {
            try {
                var cabeceraOpt = pedidoRepository.obtenerCabecera(entrega.getIdPedido());
                if (cabeceraOpt.isPresent()) {
                    var c = cabeceraOpt.get();
                    cliente = c.getCliente();
                    direccion = c.getDireccionCliente();
                    telefono = c.getTelefonoCliente();
                }
            } catch (Exception ignored) {
            }
        }

        return EntregaResponseDTO.builder()
                .idEntrega(entrega.getIdEntrega())
                .idProgramacion(entrega.getIdProgramacion())
                .idPedido(entrega.getIdPedido())
                .idEstadoEntrega(entrega.getIdEstadoEntrega())
                .estadoEntrega(nombreEstado)
                .ordenVisita(entrega.getOrdenVisita())
                .horaEstimada(entrega.getHoraEstimada())
                .horaEntregaReal(entrega.getHoraEntregaReal())
                .fotoEntregaUrl(entrega.getFotoEntregaUrl())
                .fotoGuiaUrl(entrega.getFotoGuiaUrl())
                .notas(entrega.getNotas())
                .cliente(cliente)
                .direccionCliente(direccion)
                .telefonoCliente(telefono)
                .build();
    }
}