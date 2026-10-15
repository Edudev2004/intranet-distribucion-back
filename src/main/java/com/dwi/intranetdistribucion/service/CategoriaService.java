package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.CategoriaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDTO> obtenerTodas();

    CategoriaResponseDTO obtenerPorId(Integer id);

    CategoriaResponseDTO crear(CategoriaRequestDTO categoriaRequestDTO);

    CategoriaResponseDTO actualizar(Integer id, CategoriaRequestDTO categoriaRequestDTO);

    void eliminar(Integer id);
}
