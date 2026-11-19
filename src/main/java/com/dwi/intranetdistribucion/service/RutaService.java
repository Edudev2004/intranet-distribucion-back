package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.RutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.RutaResponseDTO;

import java.util.List;

public interface RutaService {

    List<RutaResponseDTO> listar(Integer idZona, Boolean soloActivos);

    RutaResponseDTO obtenerPorId(Integer idRuta);

    RutaResponseDTO crear(RutaRequestDTO request);

    RutaResponseDTO actualizar(Integer idRuta, RutaRequestDTO request);

    void cambiarEstado(Integer idRuta, Boolean activo);
}
