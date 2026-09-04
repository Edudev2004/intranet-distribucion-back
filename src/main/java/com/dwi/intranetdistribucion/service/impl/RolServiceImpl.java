package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.RolRequestDTO;
import com.dwi.intranetdistribucion.dto.response.PermisoResponseDTO;
import com.dwi.intranetdistribucion.dto.response.RolResponseDTO;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Permiso;
import com.dwi.intranetdistribucion.model.Rol;
import com.dwi.intranetdistribucion.repository.PermisoRepository;
import com.dwi.intranetdistribucion.repository.RolRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolResponseDTO> obtenerTodos() {
        return rolRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponseDTO obtenerPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));
        return mapearADTO(rol);
    }

    @Override
    @Transactional
    public RolResponseDTO crear(RolRequestDTO request) {
        if (rolRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + request.getNombre());
        }

        Set<Permiso> permisos = obtenerPermisosPorIds(request.getPermisoIds());

        Rol rol = Rol.builder()
                .nombre(request.getNombre().toUpperCase())
                .descripcion(request.getDescripcion())
                .permisos(permisos)
                .build();

        Rol guardado = rolRepository.save(rol);
        return mapearADTO(guardado);
    }

    @Override
    @Transactional
    public RolResponseDTO actualizar(Long id, RolRequestDTO request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));

        if (request.getNombre() != null && !request.getNombre().equalsIgnoreCase(rol.getNombre())) {
            if (rolRepository.existsByNombre(request.getNombre())) {
                throw new IllegalArgumentException("Ya existe un rol con el nombre: " + request.getNombre());
            }
            rol.setNombre(request.getNombre().toUpperCase());
        }

        if (request.getDescripcion() != null) {
            rol.setDescripcion(request.getDescripcion());
        }

        if (request.getPermisoIds() != null) {
            Set<Permiso> permisos = obtenerPermisosPorIds(request.getPermisoIds());
            rol.setPermisos(permisos);
        }

        Rol actualizado = rolRepository.save(rol);
        return mapearADTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));

        // Criterio de Aceptación: No se puede eliminar un rol que tenga usuarios asignados
        if (usuarioRepository.existsByRolId(id)) {
            throw new IllegalStateException("No se puede eliminar un rol que tenga usuarios asignados.");
        }

        rolRepository.delete(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoResponseDTO> obtenerTodosLosPermisos() {
        return permisoRepository.findAll().stream()
                .map(this::mapearPermisoADTO)
                .collect(Collectors.toList());
    }

    private Set<Permiso> obtenerPermisosPorIds(Set<Long> permisoIds) {
        if (permisoIds == null || permisoIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(permisoRepository.findAllById(permisoIds));
    }

    private RolResponseDTO mapearADTO(Rol rol) {
        Set<PermisoResponseDTO> permisosDTO = rol.getPermisos().stream()
                .map(this::mapearPermisoADTO)
                .collect(Collectors.toSet());

        return RolResponseDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .permisos(permisosDTO)
                .build();
    }

    private PermisoResponseDTO mapearPermisoADTO(Permiso permiso) {
        return PermisoResponseDTO.builder()
                .id(permiso.getId())
                .nombre(permiso.getNombre())
                .descripcion(permiso.getDescripcion())
                .modulo(permiso.getModulo())
                .build();
    }
}
