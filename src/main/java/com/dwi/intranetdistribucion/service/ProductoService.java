package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ProductoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {

    List<ProductoResponseDTO> obtenerTodos(Integer idCategoria);

    ProductoResponseDTO obtenerPorId(Integer id);

    ProductoResponseDTO crear(ProductoRequestDTO request);

    ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO request);

    void eliminar(Integer id);
}
