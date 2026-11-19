package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ConductorRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ConductorResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Conductor;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.ConductorRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.ConductorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConductorServiceImpl implements ConductorService {

    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConductorResponseDTO> listar(Boolean soloActivos) {
        List<Conductor> lista = Boolean.TRUE.equals(soloActivos)
                ? conductorRepository.findByActivoTrueOrderByApellidosAscNombresAsc()
                : conductorRepository.findAllByOrderByApellidosAscNombresAsc();

        return lista.stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConductorResponseDTO obtenerPorId(Integer idConductor) {
        Conductor conductor = buscarConductor(idConductor);
        return mapToDTO(conductor);
    }

    @Override
    @Transactional
    public ConductorResponseDTO crear(ConductorRequestDTO request) {
        String dni = request.getDni().trim();
        if (conductorRepository.existsByDni(dni)) {
            throw new DuplicateResourceException("Ya existe un conductor registrado con el DNI: " + dni);
        }

        String nroLicencia = request.getNroLicencia().trim().toUpperCase();
        if (conductorRepository.existsByNroLicenciaIgnoreCase(nroLicencia)) {
            throw new DuplicateResourceException("Ya existe un conductor registrado con la licencia: " + nroLicencia);
        }

        Usuario usuario = null;
        if (request.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(request.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getIdUsuario()));
        }

        Conductor conductor = Conductor.builder()
                .usuario(usuario)
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .dni(dni)
                .nroLicencia(nroLicencia)
                .categoriaLicencia(request.getCategoriaLicencia().trim().toUpperCase())
                .fechaVencLicencia(request.getFechaVencLicencia())
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return mapToDTO(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public ConductorResponseDTO actualizar(Integer idConductor, ConductorRequestDTO request) {
        Conductor conductor = buscarConductor(idConductor);

        String dni = request.getDni().trim();
        if (conductorRepository.existsByDniAndIdConductorNot(dni, idConductor)) {
            throw new DuplicateResourceException("Ya existe otro conductor registrado con el DNI: " + dni);
        }

        String nroLicencia = request.getNroLicencia().trim().toUpperCase();
        if (conductorRepository.existsByNroLicenciaIgnoreCaseAndIdConductorNot(nroLicencia, idConductor)) {
            throw new DuplicateResourceException("Ya existe otro conductor registrado con la licencia: " + nroLicencia);
        }

        Usuario usuario = null;
        if (request.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(request.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getIdUsuario()));
        }

        conductor.setUsuario(usuario);
        conductor.setNombres(request.getNombres().trim());
        conductor.setApellidos(request.getApellidos().trim());
        conductor.setDni(dni);
        conductor.setNroLicencia(nroLicencia);
        conductor.setCategoriaLicencia(request.getCategoriaLicencia().trim().toUpperCase());
        conductor.setFechaVencLicencia(request.getFechaVencLicencia());
        conductor.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        if (request.getActivo() != null) {
            conductor.setActivo(request.getActivo());
        }

        return mapToDTO(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idConductor, Boolean activo) {
        Conductor conductor = buscarConductor(idConductor);
        conductor.setActivo(activo);
        conductorRepository.save(conductor);
    }

    private Conductor buscarConductor(Integer idConductor) {
        return conductorRepository.findById(idConductor)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con ID: " + idConductor));
    }

    private ConductorResponseDTO mapToDTO(Conductor c) {
        boolean vencida = c.getFechaVencLicencia() != null && c.getFechaVencLicencia().isBefore(LocalDate.now());

        return ConductorResponseDTO.builder()
                .idConductor(c.getIdConductor())
                .idUsuario(c.getUsuario() != null ? c.getUsuario().getIdUsuario() : null)
                .nombres(c.getNombres())
                .apellidos(c.getApellidos())
                .nombreCompleto(c.getNombres() + " " + c.getApellidos())
                .dni(c.getDni())
                .nroLicencia(c.getNroLicencia())
                .categoriaLicencia(c.getCategoriaLicencia())
                .fechaVencLicencia(c.getFechaVencLicencia())
                .licenciaVencida(vencida)
                .telefono(c.getTelefono())
                .activo(c.getActivo())
                .build();
    }
}
