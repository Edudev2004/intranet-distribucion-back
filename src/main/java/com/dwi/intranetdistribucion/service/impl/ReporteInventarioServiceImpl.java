package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.InventarioReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteInventarioDTO;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.report.InventarioExcelGenerator;
import com.dwi.intranetdistribucion.report.InventarioPdfGenerator;
import com.dwi.intranetdistribucion.repository.CategoriaRepository;
import com.dwi.intranetdistribucion.repository.InventarioRepository;
import com.dwi.intranetdistribucion.repository.projection.InventarioReporteView;
import com.dwi.intranetdistribucion.service.ReporteInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteInventarioServiceImpl implements ReporteInventarioService {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final String TODOS = "Todos";

    private final InventarioRepository inventarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final InventarioPdfGenerator inventarioPdfGenerator;
    private final InventarioExcelGenerator inventarioExcelGenerator;

    @Override
    @Transactional(readOnly = true)
    public ReporteInventarioDTO obtenerReporte(Integer idAlmacen, Integer idCategoria) {
        String almacen = idAlmacen == null ? TODOS : inventarioRepository.obtenerNombreAlmacen(idAlmacen)
                .orElseThrow(() -> new ResourceNotFoundException("Almacén no encontrado con id: " + idAlmacen));
        String categoria = idCategoria == null ? TODOS : categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + idCategoria))
                .getNombre();

        List<InventarioReporteDTO> items = inventarioRepository.obtenerReporte(idAlmacen, idCategoria).stream()
                .map(this::mapearADTO)
                .toList();
        int totalBajoStock = (int) items.stream().filter(InventarioReporteDTO::getBajoStock).count();

        return ReporteInventarioDTO.builder()
                .generadoEn(OffsetDateTime.now())
                .almacenFiltro(almacen)
                .categoriaFiltro(categoria)
                .totalRegistros(items.size())
                .totalBajoStock(totalBajoStock)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarPdf(Integer idAlmacen, Integer idCategoria) {
        ReporteInventarioDTO reporte = obtenerReporte(idAlmacen, idCategoria);
        return new ArchivoReporteDTO(nombreArchivo(reporte, "pdf"), inventarioPdfGenerator.generar(reporte));
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarExcel(Integer idAlmacen, Integer idCategoria) {
        ReporteInventarioDTO reporte = obtenerReporte(idAlmacen, idCategoria);
        return new ArchivoReporteDTO(nombreArchivo(reporte, "xlsx"), inventarioExcelGenerator.generar(reporte));
    }

    private String nombreArchivo(ReporteInventarioDTO reporte, String extension) {
        return "reporte_inventario_"
                + reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_ARCHIVO) + "." + extension;
    }

    private InventarioReporteDTO mapearADTO(InventarioReporteView fila) {
        return InventarioReporteDTO.builder()
                .idAlmacen(fila.getIdAlmacen())
                .almacen(fila.getAlmacen())
                .sede(fila.getSede())
                .idProducto(fila.getIdProducto())
                .codigo(fila.getCodigo())
                .producto(fila.getProducto())
                .categoria(fila.getCategoria())
                .unidad(fila.getUnidad())
                .stockActual(fila.getStockActual())
                .stockMinimo(fila.getStockMinimo())
                .bajoStock(fila.getStockActual().compareTo(fila.getStockMinimo()) <= 0)
                .build();
    }
}
