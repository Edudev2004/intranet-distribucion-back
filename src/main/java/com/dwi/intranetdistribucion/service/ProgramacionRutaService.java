package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ProgramacionRutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ProgramacionRutaResponseDTO;
import java.util.List;

public interface ProgramacionRutaService {
    List<ProgramacionRutaResponseDTO> listarTodas();
    ProgramacionRutaResponseDTO registrarAsignacion(ProgramacionRutaRequestDTO request);
    ProgramacionRutaResponseDTO actualizarEstado(Integer idProgramacion, Short idEstado);
}