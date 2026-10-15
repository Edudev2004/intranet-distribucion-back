package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO obtenerPorId(Integer id);

    UsuarioResponseDTO registrar(UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO usuarioRequestDTO);

    UsuarioResponseDTO cambiarEstado(Integer id, boolean activo);
}
