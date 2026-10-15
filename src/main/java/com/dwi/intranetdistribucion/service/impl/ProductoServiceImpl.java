package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ProductoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ProductoResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceInUseException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Categoria;
import com.dwi.intranetdistribucion.model.Producto;
import com.dwi.intranetdistribucion.model.UnidadMedida;
import com.dwi.intranetdistribucion.repository.CategoriaRepository;
import com.dwi.intranetdistribucion.repository.ProductoRepository;
import com.dwi.intranetdistribucion.repository.UnidadMedidaRepository;
import com.dwi.intranetdistribucion.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos(Integer idCategoria) {
        return productoRepository.findAll(Sort.by("nombre")).stream()
                .filter(p -> idCategoria == null || p.getCategoria().getIdCategoria().equals(idCategoria))
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Integer id) {
        return mapearADTO(buscarProducto(id));
    }

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        String codigo = normalizarCodigo(request.getCodigo());
        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new DuplicateResourceException("Ya existe un producto con el código: " + codigo);
        }

        Producto producto = Producto.builder()
                .codigo(codigo)
                .nombre(request.getNombre().trim())
                .categoria(buscarCategoria(request.getIdCategoria()))
                .unidadMedida(buscarUnidad(request.getIdUnidad()))
                .stockMinimo(request.getStockMinimo())
                .pesoKg(request.getPesoKg())
                .precioUnitario(request.getPrecioUnitario() != null ? request.getPrecioUnitario() : BigDecimal.ZERO)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return mapearADTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO request) {
        Producto producto = buscarProducto(id);
        String codigo = normalizarCodigo(request.getCodigo());
        if (productoRepository.existsByCodigoIgnoreCaseAndIdProductoNot(codigo, id)) {
            throw new DuplicateResourceException("Ya existe un producto con el código: " + codigo);
        }

        producto.setCodigo(codigo);
        producto.setNombre(request.getNombre().trim());
        producto.setCategoria(buscarCategoria(request.getIdCategoria()));
        producto.setUnidadMedida(buscarUnidad(request.getIdUnidad()));
        producto.setStockMinimo(request.getStockMinimo());
        producto.setPesoKg(request.getPesoKg());
        if (request.getPrecioUnitario() != null) {
            producto.setPrecioUnitario(request.getPrecioUnitario());
        }
        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        }

        return mapearADTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Producto producto = buscarProducto(id);
        long enInventario = productoRepository.contarRegistrosInventario(id);
        long enPedidos = productoRepository.contarPedidosAsociados(id);
        if (enInventario > 0 || enPedidos > 0) {
            throw new ResourceInUseException("No se puede eliminar el producto '" + producto.getCodigo()
                    + "' porque tiene " + enInventario + " registro(s) de inventario y "
                    + enPedidos + " línea(s) de pedido asociados.");
        }
        productoRepository.delete(producto);
    }

    private Producto buscarProducto(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    private Categoria buscarCategoria(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    private UnidadMedida buscarUnidad(Integer id) {
        return unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con id: " + id));
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private ProductoResponseDTO mapearADTO(Producto producto) {
        return ProductoResponseDTO.builder()
                .idProducto(producto.getIdProducto())
                .codigo(producto.getCodigo())
                .nombre(producto.getNombre())
                .idCategoria(producto.getCategoria().getIdCategoria())
                .categoria(producto.getCategoria().getNombre())
                .idUnidad(producto.getUnidadMedida().getIdUnidad())
                .unidadMedida(producto.getUnidadMedida().getCodigo())
                .stockMinimo(producto.getStockMinimo())
                .pesoKg(producto.getPesoKg())
                .precioUnitario(producto.getPrecioUnitario())
                .activo(producto.getActivo())
                .build();
    }
}
