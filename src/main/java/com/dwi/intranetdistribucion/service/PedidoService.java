package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.PedidoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteOpcionDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoResumenDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoDisponibleDTO;

import java.util.List;

public interface PedidoService {

    List<PedidoResumenDTO> obtenerTodos(Integer idEstado);

    PedidoResponseDTO obtenerPorId(Long id);

    PedidoResponseDTO crear(PedidoRequestDTO request, String emailUsuario);

    void eliminar(Long id);

    List<ClienteOpcionDTO> obtenerClientes();

    List<ProductoDisponibleDTO> obtenerProductosDisponibles(Integer idAlmacen);
}