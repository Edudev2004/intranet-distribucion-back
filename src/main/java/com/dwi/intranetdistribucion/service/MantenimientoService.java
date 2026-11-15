package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.MantenimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.MantenimientoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoMantenimientoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.VehiculoOpcionDTO;

import java.util.List;

public interface MantenimientoService {

    List<MantenimientoResponseDTO> obtenerTodos(Integer idVehiculo);

    MantenimientoResponseDTO obtenerPorId(Integer id);

    MantenimientoResponseDTO crear(MantenimientoRequestDTO request);

    MantenimientoResponseDTO actualizar(Integer id, MantenimientoRequestDTO request);

    void eliminar(Integer id);

    List<TipoMantenimientoResponseDTO> obtenerTipos();

    List<VehiculoOpcionDTO> obtenerVehiculos();
}