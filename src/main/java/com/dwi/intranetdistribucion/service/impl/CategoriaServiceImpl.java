package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.CategoriaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.CategoriaResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceInUseException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Categoria;
import com.dwi.intranetdistribucion.repository.CategoriaRepository;
import com.dwi.intranetdistribucion.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Integer id) {
        return mapearADTO(buscarCategoria(id));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        String nombre = request.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + nombre);
        }

        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .descripcion(normalizarDescripcion(request.getDescripcion()))
                .build();

        return mapearADTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Integer id, CategoriaRequestDTO request) {
        Categoria categoria = buscarCategoria(id);
        String nombre = request.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(nombre, id)) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + nombre);
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(normalizarDescripcion(request.getDescripcion()));

        return mapearADTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Categoria categoria = buscarCategoria(id);
        long productos = categoriaRepository.contarProductosAsociados(id);
        if (productos > 0) {
            throw new ResourceInUseException("No se puede eliminar la categoría '" + categoria.getNombre()
                    + "' porque tiene " + productos + " producto(s) asociado(s). "
                    + "Reasigne o elimine esos productos primero.");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarCategoria(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }
        return descripcion.trim();
    }

    private CategoriaResponseDTO mapearADTO(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}
