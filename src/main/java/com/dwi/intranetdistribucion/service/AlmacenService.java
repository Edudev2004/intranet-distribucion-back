package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.AlmacenRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AlmacenResponseDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoAlmacenResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoAlmacenResponseDTO;

import java.util.List;

public interface AlmacenService {

    List<AlmacenResponseDTO> obtenerTodos(Integer idSede);

    AlmacenResponseDTO obtenerPorId(Integer id);

    AlmacenResponseDTO crear(AlmacenRequestDTO request);

    AlmacenResponseDTO actualizar(Integer id, AlmacenRequestDTO request);

    void eliminar(Integer id);

    List<ProductoAlmacenResponseDTO> obtenerProductos(Integer id);

    List<TipoAlmacenResponseDTO> obtenerTipos();
}