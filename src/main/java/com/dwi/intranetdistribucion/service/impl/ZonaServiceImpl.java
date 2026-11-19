package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ZonaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ZonaResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Zona;
import com.dwi.intranetdistribucion.repository.ZonaRepository;
import com.dwi.intranetdistribucion.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponseDTO> listar() {
        return zonaRepository.findAllByOrderByNombreAsc().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaResponseDTO obtenerPorId(Integer idZona) {
        return mapToDTO(buscarZona(idZona));
    }

    @Override
    @Transactional
    public ZonaResponseDTO crear(ZonaRequestDTO request) {
        String nombre = request.getNombre().trim();
        if (zonaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException("Ya existe una zona de reparto con el nombre: " + nombre);
        }

        Zona zona = Zona.builder()
                .nombre(nombre)
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .build();

        return mapToDTO(zonaRepository.save(zona));
    }

    @Override
    @Transactional
    public ZonaResponseDTO actualizar(Integer idZona, ZonaRequestDTO request) {
        Zona zona = buscarZona(idZona);
        String nombre = request.getNombre().trim();

        if (zonaRepository.existsByNombreIgnoreCaseAndIdZonaNot(nombre, idZona)) {
            throw new DuplicateResourceException("Ya existe otra zona de reparto con el nombre: " + nombre);
        }

        zona.setNombre(nombre);
        zona.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);

        return mapToDTO(zonaRepository.save(zona));
    }

    @Override
    @Transactional
    public void eliminar(Integer idZona) {
        Zona zona = buscarZona(idZona);
        zonaRepository.delete(zona);
    }

    private Zona buscarZona(Integer idZona) {
        return zonaRepository.findById(idZona)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + idZona));
    }

    private ZonaResponseDTO mapToDTO(Zona z) {
        return ZonaResponseDTO.builder()
                .idZona(z.getIdZona())
                .nombre(z.getNombre())
                .descripcion(z.getDescripcion())
                .build();
    }
}
