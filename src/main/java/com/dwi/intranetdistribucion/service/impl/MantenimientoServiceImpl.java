package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.MantenimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.MantenimientoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoMantenimientoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.VehiculoOpcionDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.EstadoVehiculo;
import com.dwi.intranetdistribucion.model.MantenimientoVehiculo;
import com.dwi.intranetdistribucion.model.TipoMantenimiento;
import com.dwi.intranetdistribucion.model.Vehiculo;
import com.dwi.intranetdistribucion.repository.EstadoVehiculoRepository;
import com.dwi.intranetdistribucion.repository.MantenimientoVehiculoRepository;
import com.dwi.intranetdistribucion.repository.TipoMantenimientoRepository;
import com.dwi.intranetdistribucion.repository.VehiculoRepository;
import com.dwi.intranetdistribucion.service.MantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MantenimientoServiceImpl implements MantenimientoService {

    private static final Short ESTADO_DISPONIBLE = 1;
    private static final Short ESTADO_EN_MANTENIMIENTO = 3;

    private final MantenimientoVehiculoRepository mantenimientoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TipoMantenimientoRepository tipoMantenimientoRepository;
    private final EstadoVehiculoRepository estadoVehiculoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MantenimientoResponseDTO> obtenerTodos(Integer idVehiculo) {
        List<MantenimientoVehiculo> lista = (idVehiculo == null)
                ? mantenimientoRepository.findAllByOrderByFechaInicioDesc()
                : mantenimientoRepository.findByVehiculoIdVehiculoOrderByFechaInicioDesc(idVehiculo);
        return lista.stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MantenimientoResponseDTO obtenerPorId(Integer id) {
        return mapearADTO(buscarMantenimiento(id));
    }

    @Override
    @Transactional
    public MantenimientoResponseDTO crear(MantenimientoRequestDTO request) {
        validarFechas(request);
        Vehiculo vehiculo = buscarVehiculo(request.getIdVehiculo());
        TipoMantenimiento tipo = buscarTipo(request.getIdTipoMantenimiento());

        MantenimientoVehiculo m = MantenimientoVehiculo.builder()
                .vehiculo(vehiculo)
                .tipoMantenimiento(tipo)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .descripcion(normalizar(request.getDescripcion()))
                .costo(request.getCosto())
                .build();
        m = mantenimientoRepository.save(m);

        sincronizarEstado(vehiculo);
        return mapearADTO(m);
    }

    @Override
    @Transactional
    public MantenimientoResponseDTO actualizar(Integer id, MantenimientoRequestDTO request) {
        validarFechas(request);
        MantenimientoVehiculo m = buscarMantenimiento(id);
        Vehiculo vehiculoAnterior = m.getVehiculo();
        Vehiculo vehiculoNuevo = buscarVehiculo(request.getIdVehiculo());
        TipoMantenimiento tipo = buscarTipo(request.getIdTipoMantenimiento());

        m.setVehiculo(vehiculoNuevo);
        m.setTipoMantenimiento(tipo);
        m.setFechaInicio(request.getFechaInicio());
        m.setFechaFin(request.getFechaFin());
        m.setDescripcion(normalizar(request.getDescripcion()));
        m.setCosto(request.getCosto());
        m = mantenimientoRepository.save(m);

        sincronizarEstado(vehiculoNuevo);
        if (!vehiculoAnterior.getIdVehiculo().equals(vehiculoNuevo.getIdVehiculo())) {
            sincronizarEstado(vehiculoAnterior);
        }
        return mapearADTO(m);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        MantenimientoVehiculo m = buscarMantenimiento(id);
        Vehiculo vehiculo = m.getVehiculo();
        mantenimientoRepository.delete(m);
        mantenimientoRepository.flush();
        sincronizarEstado(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMantenimientoResponseDTO> obtenerTipos() {
        return tipoMantenimientoRepository.findAll(Sort.by("nombre")).stream()
                .map(t -> TipoMantenimientoResponseDTO.builder()
                        .idTipoMantenimiento(t.getIdTipoMantenimiento())
                        .nombre(t.getNombre())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoOpcionDTO> obtenerVehiculos() {
        return vehiculoRepository.findAllByOrderByPlacaAsc().stream()
                .map(v -> VehiculoOpcionDTO.builder()
                        .idVehiculo(v.getIdVehiculo())
                        .placa(v.getPlaca())
                        .marca(v.getMarca())
                        .modelo(v.getModelo())
                        .capacidadKg(v.getCapacidadKg())
                        .estado(v.getEstado().getNombre())
                        .build())
                .toList();
    }

    // Regla de negocio: un vehículo con mantenimiento vigente hoy queda "En mantenimiento"
    // y no puede asignarse a una ruta; al terminar o eliminarse el mantenimiento vuelve a "Disponible".
    private void sincronizarEstado(Vehiculo vehiculo) {
        LocalDate hoy = LocalDate.now();
        boolean enMantenimiento = mantenimientoRepository
                .existsByVehiculoIdVehiculoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        vehiculo.getIdVehiculo(), hoy, hoy);
        Short actual = vehiculo.getEstado().getIdEstadoVehiculo();

        if (enMantenimiento && !ESTADO_EN_MANTENIMIENTO.equals(actual)) {
            vehiculo.setEstado(buscarEstado(ESTADO_EN_MANTENIMIENTO));
            vehiculoRepository.save(vehiculo);
        } else if (!enMantenimiento && ESTADO_EN_MANTENIMIENTO.equals(actual)) {
            vehiculo.setEstado(buscarEstado(ESTADO_DISPONIBLE));
            vehiculoRepository.save(vehiculo);
        }
    }

    private void validarFechas(MantenimientoRequestDTO request) {
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private MantenimientoVehiculo buscarMantenimiento(Integer id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento no encontrado con id: " + id));
    }

    private Vehiculo buscarVehiculo(Integer id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con id: " + id));
    }

    private TipoMantenimiento buscarTipo(Short id) {
        return tipoMantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de mantenimiento no encontrado con id: " + id));
    }

    private EstadoVehiculo buscarEstado(Short id) {
        return estadoVehiculoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Falta el estado de vehículo " + id
                        + " en el catálogo estado_vehiculo"));
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private MantenimientoResponseDTO mapearADTO(MantenimientoVehiculo m) {
        LocalDate hoy = LocalDate.now();
        boolean vigente = !hoy.isBefore(m.getFechaInicio()) && !hoy.isAfter(m.getFechaFin());
        return MantenimientoResponseDTO.builder()
                .idMantenimiento(m.getIdMantenimiento())
                .idVehiculo(m.getVehiculo().getIdVehiculo())
                .placa(m.getVehiculo().getPlaca())
                .estadoVehiculo(m.getVehiculo().getEstado().getNombre())
                .idTipoMantenimiento(m.getTipoMantenimiento().getIdTipoMantenimiento())
                .tipoMantenimiento(m.getTipoMantenimiento().getNombre())
                .fechaInicio(m.getFechaInicio())
                .fechaFin(m.getFechaFin())
                .descripcion(m.getDescripcion())
                .costo(m.getCosto())
                .vigente(vigente)
                .build();
    }
}