package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Rol;
import com.dwi.intranetdistribucion.model.Sede;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.RolRepository;
import com.dwi.intranetdistribucion.repository.SedeRepository;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return mapearADTO(buscarUsuario(id));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO request) {
        String correo = normalizar(request.getCorreo());
        String username = normalizar(request.getUsername());

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("La contraseña es obligatoria");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new DuplicateResourceException("El correo ya se encuentra registrado: " + correo);
        }
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("El username ya se encuentra registrado: " + username);
        }

        Usuario usuario = Usuario.builder()
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .username(username)
                .correo(correo)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(buscarRol(request.getIdRol()))
                .sede(buscarSede(request.getIdSede()))
                .build();

        return mapearADTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO request) {
        Usuario usuario = buscarUsuario(id);
        String correo = normalizar(request.getCorreo());
        String username = normalizar(request.getUsername());

        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(correo, id)) {
            throw new DuplicateResourceException("El correo ya se encuentra registrado: " + correo);
        }
        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdUsuarioNot(username, id)) {
            throw new DuplicateResourceException("El username ya se encuentra registrado: " + username);
        }

        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setCorreo(correo);
        usuario.setUsername(username);
        usuario.setRol(buscarRol(request.getIdRol()));
        usuario.setSede(buscarSede(request.getIdSede()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return mapearADTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Integer id, boolean activo) {
        Usuario usuario = buscarUsuario(id);
        usuario.setActivo(activo);
        return mapearADTO(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    private Rol buscarRol(Short idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + idRol));
    }

    private Sede buscarSede(Integer idSede) {
        return sedeRepository.findById(idSede)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + idSede));
    }

    private String normalizar(String valor) {
        return valor.trim().toLowerCase();
    }

    private UsuarioResponseDTO mapearADTO(Usuario usuario) {
        Rol rol = usuario.getRol();
        Sede sede = usuario.getSede();
        return UsuarioResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .username(usuario.getUsername())
                .correo(usuario.getCorreo())
                .idRol(rol != null ? rol.getIdRol() : null)
                .rol(rol != null ? rol.getNombre() : null)
                .idSede(sede != null ? sede.getIdSede() : null)
                .sede(sede != null ? sede.getNombre() : null)
                .activo(usuario.getActivo())
                .intentosFallidos(usuario.getIntentosFallidos())
                .bloqueado(usuario.getBloqueado())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .creadoEn(usuario.getCreadoEn())
                .build();
    }
}
