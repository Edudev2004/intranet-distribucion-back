package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ZonaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ZonaResponseDTO;

import java.util.List;

public interface ZonaService {

    List<ZonaResponseDTO> listar();

    ZonaResponseDTO obtenerPorId(Integer idZona);

    ZonaResponseDTO crear(ZonaRequestDTO request);

    ZonaResponseDTO actualizar(Integer idZona, ZonaRequestDTO request);

    void eliminar(Integer idZona);
}
