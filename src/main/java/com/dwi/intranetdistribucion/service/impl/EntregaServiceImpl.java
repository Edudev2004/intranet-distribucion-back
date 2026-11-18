package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ActualizarEntregaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EntregaResponseDTO;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Entrega;
import com.dwi.intranetdistribucion.repository.EntregaRepository;
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

        // Actualizamos directamente el ID del estado recibido
        entrega.setIdEstadoEntrega(request.getIdEstadoEntrega());

        // Si el estado es el correspondiente a entregado (por ejemplo, id = 2 o según convención de la BD), 
        // registramos la hora real automáticamente si no estaba registrada.
        // O si manejas el ID directamente: (Ej: si id 2 es Entregado)
        if (request.getIdEstadoEntrega().equals((short) 2) && entrega.getHoraEntregaReal() == null) {
            entrega.setHoraEntregaReal(OffsetDateTime.now());
        }

        entrega = entregaRepository.save(entrega);
        return mapearRespuesta(entrega);
    }

    private EntregaResponseDTO mapearRespuesta(Entrega entrega) {
        // Obtenemos el nombre del estado mediante la consulta del repositorio
        String nombreEstado = entregaRepository.obtenerNombreEstadoEntrega(entrega.getIdEstadoEntrega().intValue())
                .orElse("Desconocido");

        return EntregaResponseDTO.builder()
                .idEntrega(entrega.getIdEntrega())
                .idProgramacion(entrega.getIdProgramacion())
                .idPedido(entrega.getIdPedido())
                .idEstadoEntrega(entrega.getIdEstadoEntrega())
                .estadoEntrega(nombreEstado)
                .ordenVisita(entrega.getOrdenVisita())
                .horaEstimada(entrega.getHoraEstimada())
                .horaEntregaReal(entrega.getHoraEntregaReal())
                .build();
    }
}