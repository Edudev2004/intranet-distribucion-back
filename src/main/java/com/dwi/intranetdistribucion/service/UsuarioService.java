package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO obtenerPorId(Long id);

    UsuarioResponseDTO crear(UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO usuarioRequestDTO);

    void eliminar(Long id);
}
