package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReportePedidoDTO;
import com.dwi.intranetdistribucion.dto.response.ReportePedidoItemDTO;
import com.dwi.intranetdistribucion.report.PedidoExcelGenerator;
import com.dwi.intranetdistribucion.report.PedidoPdfGenerator;
import com.dwi.intranetdistribucion.repository.PedidoRepository;
import com.dwi.intranetdistribucion.repository.projection.PedidoResumenView;
import com.dwi.intranetdistribucion.service.ReportePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportePedidoServiceImpl implements ReportePedidoService {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final String TODOS = "Todos";

    private final PedidoRepository pedidoRepository;
    private final PedidoPdfGenerator pedidoPdfGenerator;
    private final PedidoExcelGenerator pedidoExcelGenerator;

    @Override
    @Transactional(readOnly = true)
    public ReportePedidoDTO obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin, Integer idEstado, Integer idCliente) {
        String estado = idEstado == null ? TODOS : pedidoRepository.obtenerNombreEstado(idEstado).orElse(TODOS);
        String cliente = idCliente == null ? TODOS : pedidoRepository.obtenerNombreCliente(idCliente).orElse(TODOS);

        List<PedidoResumenView> pedidos = pedidoRepository.obtenerReportePedidos(fechaInicio, fechaFin, idEstado, idCliente);

        List<ReportePedidoItemDTO> items = pedidos.stream()
                .map(p -> ReportePedidoItemDTO.builder()
                        .idPedido(p.getIdPedido())
                        .fechaPedido(p.getFechaPedido())
                        .cliente(p.getCliente())
                        .almacen(p.getAlmacen())
                        .idEstadoPedido(p.getIdEstadoPedido())
                        .estado(p.getEstado())
                        .total(p.getTotal())
                        .build())
                .toList();

        BigDecimal montoTotal = items.stream()
                .map(ReportePedidoItemDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReportePedidoDTO.builder()
                .generadoEn(OffsetDateTime.now())
                .fechaInicioFiltro(fechaInicio)
                .fechaFinFiltro(fechaFin)
                .estadoFiltro(estado)
                .clienteFiltro(cliente)
                .totalPedidos(items.size())
                .montoTotalGeneral(montoTotal)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarPdf(LocalDate fechaInicio, LocalDate fechaFin, Integer idEstado, Integer idCliente) {
        ReportePedidoDTO reporte = obtenerReporte(fechaInicio, fechaFin, idEstado, idCliente);
        return new ArchivoReporteDTO(nombreArchivo(reporte, "pdf"), pedidoPdfGenerator.generar(reporte));
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarExcel(LocalDate fechaInicio, LocalDate fechaFin, Integer idEstado, Integer idCliente) {
        ReportePedidoDTO reporte = obtenerReporte(fechaInicio, fechaFin, idEstado, idCliente);
        return new ArchivoReporteDTO(nombreArchivo(reporte, "xlsx"), pedidoExcelGenerator.generar(reporte));
    }

    private String nombreArchivo(ReportePedidoDTO reporte, String extension) {
        return "reporte_pedidos_"
                + reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_ARCHIVO) + "." + extension;
    }
}
