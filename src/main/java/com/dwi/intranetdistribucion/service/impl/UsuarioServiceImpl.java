package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Usuario;
import com.dwi.intranetdistribucion.repository.UsuarioRepository;
import com.dwi.intranetdistribucion.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return mapearADTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return mapearADTO(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());

        Usuario actualizado = usuarioRepository.save(usuario);
        return mapearADTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuarioRepository.delete(usuario);
    }

    private UsuarioResponseDTO mapearADTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .esActivo(usuario.getEsActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}
