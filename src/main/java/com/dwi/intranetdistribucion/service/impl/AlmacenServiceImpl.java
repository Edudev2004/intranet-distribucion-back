package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.AlmacenRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AlmacenResponseDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoAlmacenResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoAlmacenResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceInUseException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Almacen;
import com.dwi.intranetdistribucion.model.Sede;
import com.dwi.intranetdistribucion.model.TipoAlmacen;
import com.dwi.intranetdistribucion.repository.AlmacenRepository;
import com.dwi.intranetdistribucion.repository.SedeRepository;
import com.dwi.intranetdistribucion.repository.TipoAlmacenRepository;
import com.dwi.intranetdistribucion.service.AlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final SedeRepository sedeRepository;
    private final TipoAlmacenRepository tipoAlmacenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlmacenResponseDTO> obtenerTodos(Integer idSede) {
        List<Almacen> almacenes = (idSede == null)
                ? almacenRepository.findAllByOrderByNombreAsc()
                : almacenRepository.findBySedeIdSedeOrderByNombreAsc(idSede);
        return almacenes.stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlmacenResponseDTO obtenerPorId(Integer id) {
        return mapearADTO(buscarAlmacen(id));
    }

    @Override
    @Transactional
    public AlmacenResponseDTO crear(AlmacenRequestDTO request) {
        Sede sede = buscarSede(request.getIdSede());
        TipoAlmacen tipo = buscarTipo(request.getIdTipoAlmacen());
        String nombre = request.getNombre().trim();

        if (almacenRepository.existsBySedeIdSedeAndNombreIgnoreCase(sede.getIdSede(), nombre)) {
            throw new DuplicateResourceException("Ya existe un almacén '" + nombre + "' en la sede " + sede.getNombre());
        }

        Almacen almacen = Almacen.builder()
                .sede(sede)
                .nombre(nombre)
                .tipoAlmacen(tipo)
                .capacidadMaxima(request.getCapacidadMaxima())
                .ubicacion(normalizar(request.getUbicacion()))
                .activo(request.getActivo() == null ? Boolean.TRUE : request.getActivo())
                .build();

        return mapearADTO(almacenRepository.save(almacen));
    }

    @Override
    @Transactional
    public AlmacenResponseDTO actualizar(Integer id, AlmacenRequestDTO request) {
        Almacen almacen = buscarAlmacen(id);
        Sede sede = buscarSede(request.getIdSede());
        TipoAlmacen tipo = buscarTipo(request.getIdTipoAlmacen());
        String nombre = request.getNombre().trim();

        if (almacenRepository.existsBySedeIdSedeAndNombreIgnoreCaseAndIdAlmacenNot(sede.getIdSede(), nombre, id)) {
            throw new DuplicateResourceException("Ya existe un almacén '" + nombre + "' en la sede " + sede.getNombre());
        }

        almacen.setSede(sede);
        almacen.setNombre(nombre);
        almacen.setTipoAlmacen(tipo);
        almacen.setCapacidadMaxima(request.getCapacidadMaxima());
        almacen.setUbicacion(normalizar(request.getUbicacion()));
        if (request.getActivo() != null) {
            almacen.setActivo(request.getActivo());
        }

        return mapearADTO(almacenRepository.save(almacen));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Almacen almacen = buscarAlmacen(id);
        long dependencias = almacenRepository.contarDependencias(id);
        if (dependencias > 0) {
            throw new ResourceInUseException("No se puede eliminar el almacén '" + almacen.getNombre()
                    + "' porque tiene inventario, pedidos o movimientos asociados. Desactívelo en su lugar.");
        }
        almacenRepository.delete(almacen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoAlmacenResponseDTO> obtenerProductos(Integer id) {
        buscarAlmacen(id);
        return almacenRepository.obtenerProductos(id).stream()
                .map(v -> ProductoAlmacenResponseDTO.builder()
                        .idProducto(v.getIdProducto())
                        .codigo(v.getCodigo())
                        .producto(v.getProducto())
                        .categoria(v.getCategoria())
                        .stockActual(v.getStockActual())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoAlmacenResponseDTO> obtenerTipos() {
        return tipoAlmacenRepository.findAll(Sort.by("nombre")).stream()
                .map(t -> TipoAlmacenResponseDTO.builder()
                        .idTipoAlmacen(t.getIdTipoAlmacen())
                        .nombre(t.getNombre())
                        .build())
                .toList();
    }

    private Almacen buscarAlmacen(Integer id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con id: " + id));
    }

    private Sede buscarSede(Integer id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con id: " + id));
    }

    private TipoAlmacen buscarTipo(Short id) {
        return tipoAlmacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de almacén no encontrado con id: " + id));
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private AlmacenResponseDTO mapearADTO(Almacen a) {
        return AlmacenResponseDTO.builder()
                .idAlmacen(a.getIdAlmacen())
                .idSede(a.getSede().getIdSede())
                .sede(a.getSede().getNombre())
                .nombre(a.getNombre())
                .idTipoAlmacen(a.getTipoAlmacen().getIdTipoAlmacen())
                .tipoAlmacen(a.getTipoAlmacen().getNombre())
                .capacidadMaxima(a.getCapacidadMaxima())
                .ubicacion(a.getUbicacion())
                .activo(a.getActivo())
                .build();
    }
}