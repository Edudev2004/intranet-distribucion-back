package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.VehiculoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.EstadoVehiculoDTO;
import com.dwi.intranetdistribucion.dto.response.VehiculoResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.EstadoVehiculo;
import com.dwi.intranetdistribucion.model.Vehiculo;
import com.dwi.intranetdistribucion.repository.EstadoVehiculoRepository;
import com.dwi.intranetdistribucion.repository.VehiculoRepository;
import com.dwi.intranetdistribucion.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final EstadoVehiculoRepository estadoVehiculoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listar(Short idEstado) {
        List<Vehiculo> lista = (idEstado != null)
                ? vehiculoRepository.findByEstado_IdEstadoVehiculoOrderByPlacaAsc(idEstado)
                : vehiculoRepository.findAllByOrderByPlacaAsc();

        return lista.stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerPorId(Integer idVehiculo) {
        Vehiculo vehiculo = buscarVehiculo(idVehiculo);
        return mapToDTO(vehiculo);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO request) {
        String placaLimpia = request.getPlaca().trim().toUpperCase();
        if (vehiculoRepository.existsByPlacaIgnoreCase(placaLimpia)) {
            throw new DuplicateResourceException("Ya existe un vehículo registrado con la placa: " + placaLimpia);
        }

        EstadoVehiculo estado = estadoVehiculoRepository.findById(request.getIdEstadoVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Estado de vehículo no encontrado con ID: " + request.getIdEstadoVehiculo()));

        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placaLimpia)
                .marca(request.getMarca() != null ? request.getMarca().trim() : null)
                .modelo(request.getModelo() != null ? request.getModelo().trim() : null)
                .capacidadKg(request.getCapacidadKg())
                .estado(estado)
                .build();

        return mapToDTO(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizar(Integer idVehiculo, VehiculoRequestDTO request) {
        Vehiculo vehiculo = buscarVehiculo(idVehiculo);
        String placaLimpia = request.getPlaca().trim().toUpperCase();

        if (vehiculoRepository.existsByPlacaIgnoreCaseAndIdVehiculoNot(placaLimpia, idVehiculo)) {
            throw new DuplicateResourceException("Ya existe otro vehículo registrado con la placa: " + placaLimpia);
        }

        EstadoVehiculo estado = estadoVehiculoRepository.findById(request.getIdEstadoVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Estado de vehículo no encontrado con ID: " + request.getIdEstadoVehiculo()));

        vehiculo.setPlaca(placaLimpia);
        vehiculo.setMarca(request.getMarca() != null ? request.getMarca().trim() : null);
        vehiculo.setModelo(request.getModelo() != null ? request.getModelo().trim() : null);
        vehiculo.setCapacidadKg(request.getCapacidadKg());
        vehiculo.setEstado(estado);

        return mapToDTO(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idVehiculo, Short idEstado) {
        Vehiculo vehiculo = buscarVehiculo(idVehiculo);
        EstadoVehiculo estado = estadoVehiculoRepository.findById(idEstado)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de vehículo no encontrado con ID: " + idEstado));
        vehiculo.setEstado(estado);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    @Transactional
    public void eliminar(Integer idVehiculo) {
        Vehiculo vehiculo = buscarVehiculo(idVehiculo);
        vehiculoRepository.delete(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoVehiculoDTO> listarEstados() {
        return estadoVehiculoRepository.findAll().stream()
                .map(e -> EstadoVehiculoDTO.builder()
                        .idEstadoVehiculo(e.getIdEstadoVehiculo())
                        .nombre(e.getNombre())
                        .build())
                .toList();
    }

    private Vehiculo buscarVehiculo(Integer idVehiculo) {
        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + idVehiculo));
    }

    private VehiculoResponseDTO mapToDTO(Vehiculo v) {
        return VehiculoResponseDTO.builder()
                .idVehiculo(v.getIdVehiculo())
                .placa(v.getPlaca())
                .marca(v.getMarca())
                .modelo(v.getModelo())
                .capacidadKg(v.getCapacidadKg())
                .idEstadoVehiculo(v.getEstado().getIdEstadoVehiculo())
                .nombreEstado(v.getEstado().getNombre())
                .build();
    }
}
