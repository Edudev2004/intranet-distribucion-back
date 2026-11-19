package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.RutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.RutaResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Ruta;
import com.dwi.intranetdistribucion.model.Zona;
import com.dwi.intranetdistribucion.repository.RutaRepository;
import com.dwi.intranetdistribucion.repository.ZonaRepository;
import com.dwi.intranetdistribucion.service.RutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RutaServiceImpl implements RutaService {

    private final RutaRepository rutaRepository;
    private final ZonaRepository zonaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RutaResponseDTO> listar(Integer idZona, Boolean soloActivos) {
        List<Ruta> lista;
        if (idZona != null) {
            lista = Boolean.TRUE.equals(soloActivos)
                    ? rutaRepository.findByZona_IdZonaAndActivoTrueOrderByNombreAsc(idZona)
                    : rutaRepository.findByZona_IdZonaOrderByNombreAsc(idZona);
        } else {
            lista = Boolean.TRUE.equals(soloActivos)
                    ? rutaRepository.findByActivoTrueOrderByNombreAsc()
                    : rutaRepository.findAllByOrderByNombreAsc();
        }

        return lista.stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutaResponseDTO obtenerPorId(Integer idRuta) {
        return mapToDTO(buscarRuta(idRuta));
    }

    @Override
    @Transactional
    public RutaResponseDTO crear(RutaRequestDTO request) {
        String nombre = request.getNombre().trim();
        if (rutaRepository.existsByNombreIgnoreCaseAndZona_IdZona(nombre, request.getIdZona())) {
            throw new DuplicateResourceException("Ya existe una ruta con ese nombre en la zona seleccionada");
        }

        Zona zona = zonaRepository.findById(request.getIdZona())
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + request.getIdZona()));

        Ruta ruta = Ruta.builder()
                .nombre(nombre)
                .zona(zona)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return mapToDTO(rutaRepository.save(ruta));
    }

    @Override
    @Transactional
    public RutaResponseDTO actualizar(Integer idRuta, RutaRequestDTO request) {
        Ruta ruta = buscarRuta(idRuta);
        String nombre = request.getNombre().trim();

        if (rutaRepository.existsByNombreIgnoreCaseAndZona_IdZonaAndIdRutaNot(nombre, request.getIdZona(), idRuta)) {
            throw new DuplicateResourceException("Ya existe otra ruta con ese nombre en la zona seleccionada");
        }

        Zona zona = zonaRepository.findById(request.getIdZona())
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + request.getIdZona()));

        ruta.setNombre(nombre);
        ruta.setZona(zona);
        if (request.getActivo() != null) {
            ruta.setActivo(request.getActivo());
        }

        return mapToDTO(rutaRepository.save(ruta));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idRuta, Boolean activo) {
        Ruta ruta = buscarRuta(idRuta);
        ruta.setActivo(activo);
        rutaRepository.save(ruta);
    }

    private Ruta buscarRuta(Integer idRuta) {
        return rutaRepository.findById(idRuta)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta no encontrada con ID: " + idRuta));
    }

    private RutaResponseDTO mapToDTO(Ruta r) {
        return RutaResponseDTO.builder()
                .idRuta(r.getIdRuta())
                .nombre(r.getNombre())
                .idZona(r.getZona().getIdZona())
                .nombreZona(r.getZona().getNombre())
                .activo(r.getActivo())
                .build();
    }
}
