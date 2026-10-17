package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.SedeRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ResponsableSedeResponseDTO;
import com.dwi.intranetdistribucion.dto.response.SedeResponseDTO;

import java.util.List;

public interface SedeService {

    List<SedeResponseDTO> obtenerTodas(Boolean soloActivos);

    SedeResponseDTO obtenerPorId(Integer id);

    SedeResponseDTO crear(SedeRequestDTO request);

    SedeResponseDTO actualizar(Integer id, SedeRequestDTO request);

    void eliminar(Integer id);

    SedeResponseDTO cambiarEstado(Integer id, boolean activo);

    List<ResponsableSedeResponseDTO> obtenerResponsablesDisponibles();
}
