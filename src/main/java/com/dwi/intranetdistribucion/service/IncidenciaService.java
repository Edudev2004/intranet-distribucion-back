package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.IncidenciaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.IncidenciaResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoIncidenciaResponseDTO;

import java.util.List;

public interface IncidenciaService {

    IncidenciaResponseDTO registrar(IncidenciaRequestDTO incidenciaRequestDTO);

    List<IncidenciaResponseDTO> listar(Long idEntrega, Long idPedido);

    IncidenciaResponseDTO obtenerPorId(Long id);

    List<TipoIncidenciaResponseDTO> listarTipos();
}
