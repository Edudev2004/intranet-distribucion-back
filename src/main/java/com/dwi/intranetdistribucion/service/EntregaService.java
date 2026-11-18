package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ActualizarEntregaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EntregaResponseDTO;
import java.util.List;

public interface EntregaService {
    List<EntregaResponseDTO> listarPorProgramacion(Integer idProgramacion);
    EntregaResponseDTO actualizarEstadoEntrega(Long idEntrega, ActualizarEntregaRequestDTO request);
}