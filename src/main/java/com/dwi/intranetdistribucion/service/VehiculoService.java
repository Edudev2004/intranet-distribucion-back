package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.VehiculoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EstadoVehiculoDTO;
import com.dwi.intranetdistribucion.dto.response.VehiculoResponseDTO;

import java.util.List;

public interface VehiculoService {

    List<VehiculoResponseDTO> listar(Short idEstado);

    VehiculoResponseDTO obtenerPorId(Integer idVehiculo);

    VehiculoResponseDTO crear(VehiculoRequestDTO request);

    VehiculoResponseDTO actualizar(Integer idVehiculo, VehiculoRequestDTO request);

    void cambiarEstado(Integer idVehiculo, Short idEstado);

    void eliminar(Integer idVehiculo);

    List<EstadoVehiculoDTO> listarEstados();
}
