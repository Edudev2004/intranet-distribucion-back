package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.RolRequestDTO;
import com.dwi.intranetdistribucion.dto.response.PermisoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.RolResponseDTO;

import java.util.List;

public interface RolService {

    List<RolResponseDTO> obtenerTodos();

    RolResponseDTO obtenerPorId(Long id);

    RolResponseDTO crear(RolRequestDTO request);

    RolResponseDTO actualizar(Long id, RolRequestDTO request);

    void eliminar(Long id);

    List<PermisoResponseDTO> obtenerTodosLosPermisos();
}
