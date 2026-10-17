package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.SedeRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ResponsableSedeResponseDTO;
import com.dwi.intranetdistribucion.dto.response.SedeResponseDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceInUseException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Sede;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.SedeRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.SedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SedeResponseDTO> obtenerTodas(Boolean soloActivos) {
        List<Sede> sedes = Boolean.TRUE.equals(soloActivos)
                ? sedeRepository.findByActivoTrueWithResponsable()
                : sedeRepository.findAllWithResponsable();

        return sedes.stream()
                .map(s -> mapearADTO(
                        s,
                        sedeRepository.contarUsuariosAsociados(s.getIdSede()),
                        sedeRepository.contarAlmacenesAsociados(s.getIdSede())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SedeResponseDTO obtenerPorId(Integer id) {
        Sede sede = buscarSedeConResponsable(id);
        long totalUsuarios = sedeRepository.contarUsuariosAsociados(id);
        long totalAlmacenes = sedeRepository.contarAlmacenesAsociados(id);
        return mapearADTO(sede, totalUsuarios, totalAlmacenes);
    }

    @Override
    @Transactional
    public SedeResponseDTO crear(SedeRequestDTO request) {
        String nombre = request.getNombre().trim();
        String direccion = request.getDireccion().trim();

        if (sedeRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException("Ya existe una sede con el nombre '" + nombre + "'");
        }

        Usuario responsable = null;
        if (request.getIdResponsable() != null) {
            responsable = buscarResponsable(request.getIdResponsable());
        }

        Sede sede = Sede.builder()
                .nombre(nombre)
                .direccion(direccion)
                .responsable(responsable)
                .activo(request.getActivo() == null ? Boolean.TRUE : request.getActivo())
                .build();

        Sede guardada = sedeRepository.save(sede);
        return mapearADTO(guardada, 0L, 0L);
    }

    @Override
    @Transactional
    public SedeResponseDTO actualizar(Integer id, SedeRequestDTO request) {
        Sede sede = buscarSede(id);
        String nombre = request.getNombre().trim();
        String direccion = request.getDireccion().trim();

        if (sedeRepository.existsByNombreIgnoreCaseAndIdSedeNot(nombre, id)) {
            throw new DuplicateResourceException("Ya existe otra sede con el nombre '" + nombre + "'");
        }

        Usuario responsable = null;
        if (request.getIdResponsable() != null) {
            responsable = buscarResponsable(request.getIdResponsable());
        }

        sede.setNombre(nombre);
        sede.setDireccion(direccion);
        sede.setResponsable(responsable);
        if (request.getActivo() != null) {
            sede.setActivo(request.getActivo());
        }

        Sede actualizada = sedeRepository.save(sede);
        long totalUsuarios = sedeRepository.contarUsuariosAsociados(id);
        long totalAlmacenes = sedeRepository.contarAlmacenesAsociados(id);
        return mapearADTO(actualizada, totalUsuarios, totalAlmacenes);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Sede sede = buscarSede(id);

        long usuariosAsociados = sedeRepository.contarUsuariosAsociados(id);
        if (usuariosAsociados > 0) {
            throw new ResourceInUseException("No se puede eliminar la sede '" + sede.getNombre()
                    + "' porque tiene " + usuariosAsociados + " usuario(s) asociado(s). Reasigne o desactive los usuarios antes de eliminar, o desactive la sede.");
        }

        long inventarioAsociado = sedeRepository.contarInventarioAsociado(id);
        if (inventarioAsociado > 0) {
            throw new ResourceInUseException("No se puede eliminar la sede '" + sede.getNombre()
                    + "' porque sus almacenes tienen inventario registrado. Vacíe el inventario o desactive la sede en su lugar.");
        }

        long almacenesAsociados = sedeRepository.contarAlmacenesAsociados(id);
        if (almacenesAsociados > 0) {
            throw new ResourceInUseException("No se puede eliminar la sede '" + sede.getNombre()
                    + "' porque tiene " + almacenesAsociados + " almacén(es) asociado(s). Elimine o reasigne los almacenes primero, o desactive la sede en su lugar.");
        }

        sedeRepository.delete(sede);
    }

    @Override
    @Transactional
    public SedeResponseDTO cambiarEstado(Integer id, boolean activo) {
        Sede sede = buscarSedeConResponsable(id);
        sede.setActivo(activo);
        Sede actualizada = sedeRepository.save(sede);
        long totalUsuarios = sedeRepository.contarUsuariosAsociados(id);
        long totalAlmacenes = sedeRepository.contarAlmacenesAsociados(id);
        return mapearADTO(actualizada, totalUsuarios, totalAlmacenes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponsableSedeResponseDTO> obtenerResponsablesDisponibles() {
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()) && !Boolean.TRUE.equals(u.getBloqueado()))
                .map(u -> ResponsableSedeResponseDTO.builder()
                        .idUsuario(u.getIdUsuario())
                        .nombreCompleto(u.getNombres() + " " + u.getApellidos())
                        .username(u.getUsername())
                        .correo(u.getCorreo())
                        .rol(u.getRol() != null ? u.getRol().getNombre() : null)
                        .build())
                .sorted(Comparator.comparing(ResponsableSedeResponseDTO::getNombreCompleto))
                .toList();
    }

    private Sede buscarSede(Integer id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + id));
    }

    private Sede buscarSedeConResponsable(Integer id) {
        return sedeRepository.findByIdWithResponsable(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + id));
    }

    private Usuario buscarResponsable(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado con id: " + idUsuario));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadRequestException("No se puede asignar como responsable a un usuario inactivo");
        }
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            throw new BadRequestException("No se puede asignar como responsable a un usuario bloqueado");
        }
        return usuario;
    }

    private SedeResponseDTO mapearADTO(Sede s, Long totalUsuarios, Long totalAlmacenes) {
        Usuario r = s.getResponsable();
        return SedeResponseDTO.builder()
                .idSede(s.getIdSede())
                .nombre(s.getNombre())
                .direccion(s.getDireccion())
                .idResponsable(r != null ? r.getIdUsuario() : null)
                .nombreResponsable(r != null ? (r.getNombres() + " " + r.getApellidos()) : null)
                .correoResponsable(r != null ? r.getCorreo() : null)
                .activo(s.getActivo())
                .totalUsuarios(totalUsuarios)
                .totalAlmacenes(totalAlmacenes)
                .build();
    }
}
