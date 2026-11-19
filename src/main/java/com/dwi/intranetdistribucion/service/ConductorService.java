package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ConductorRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ConductorResponseDTO;

import java.util.List;

public interface ConductorService {

    List<ConductorResponseDTO> listar(Boolean soloActivos);

    ConductorResponseDTO obtenerPorId(Integer idConductor);

    ConductorResponseDTO crear(ConductorRequestDTO request);

    ConductorResponseDTO actualizar(Integer idConductor, ConductorRequestDTO request);

    void cambiarEstado(Integer idConductor, Boolean activo);
}
