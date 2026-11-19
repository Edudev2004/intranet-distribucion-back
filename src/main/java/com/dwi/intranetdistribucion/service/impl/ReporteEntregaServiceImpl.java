package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.response.ArchivoReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteEntregaDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteEntregaItemDTO;
import com.dwi.intranetdistribucion.report.EntregaExcelGenerator;
import com.dwi.intranetdistribucion.report.EntregaPdfGenerator;
import com.dwi.intranetdistribucion.repository.EntregaRepository;
import com.dwi.intranetdistribucion.repository.projection.EntregaReporteView;
import com.dwi.intranetdistribucion.service.ReporteEntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteEntregaServiceImpl implements ReporteEntregaService {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final String TODOS = "Todos";

    private final EntregaRepository entregaRepository;
    private final EntregaPdfGenerator entregaPdfGenerator;
    private final EntregaExcelGenerator entregaExcelGenerator;

    @Override
    @Transactional(readOnly = true)
    public ReporteEntregaDTO obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin, Short idEstado) {
        String estado = idEstado == null ? TODOS : entregaRepository.obtenerNombreEstadoEntrega(idEstado.intValue()).orElse(TODOS);

        List<EntregaReporteView> entregas = entregaRepository.obtenerReporteEntregas(fechaInicio, fechaFin, idEstado);

        List<ReporteEntregaItemDTO> items = entregas.stream()
                .map(this::mapearADTO)
                .toList();

        int total = items.size();
        int aTiempo = (int) items.stream().filter(i -> "A TIEMPO".equals(i.getCumplimiento())).count();
        int conRetraso = (int) items.stream().filter(i -> "CON RETRASO".equals(i.getCumplimiento())).count();
        int pendientes = (int) items.stream().filter(i -> "PENDIENTE".equals(i.getCumplimiento())).count();

        BigDecimal porcentaje = total > 0
                ? BigDecimal.valueOf(aTiempo).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ReporteEntregaDTO.builder()
                .generadoEn(OffsetDateTime.now())
                .fechaInicioFiltro(fechaInicio)
                .fechaFinFiltro(fechaFin)
                .estadoFiltro(estado)
                .totalEntregas(total)
                .totalATiempo(aTiempo)
                .totalConRetraso(conRetraso)
                .totalPendientes(pendientes)
                .porcentajeCumplimiento(porcentaje)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarPdf(LocalDate fechaInicio, LocalDate fechaFin, Short idEstado) {
        ReporteEntregaDTO reporte = obtenerReporte(fechaInicio, fechaFin, idEstado);
        return new ArchivoReporteDTO(nombreArchivo(reporte, "pdf"), entregaPdfGenerator.generar(reporte));
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarExcel(LocalDate fechaInicio, LocalDate fechaFin, Short idEstado) {
        ReporteEntregaDTO reporte = obtenerReporte(fechaInicio, fechaFin, idEstado);
        return new ArchivoReporteDTO(nombreArchivo(reporte, "xlsx"), entregaExcelGenerator.generar(reporte));
    }

    private String nombreArchivo(ReporteEntregaDTO reporte, String extension) {
        return "reporte_entregas_"
                + reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_ARCHIVO) + "." + extension;
    }

    private ReporteEntregaItemDTO mapearADTO(EntregaReporteView e) {
        String estimadaStr = e.getHoraEstimada() != null
                ? e.getHoraEstimada().atZoneSameInstant(ZONA_PERU).format(FORMATO_HORA) : "-";
        String realStr = e.getHoraEntregaReal() != null
                ? e.getHoraEntregaReal().atZoneSameInstant(ZONA_PERU).format(FORMATO_HORA) : "-";

        String cumplimiento = "PENDIENTE";
        if (e.getHoraEntregaReal() != null) {
            if (e.getHoraEstimada() != null && e.getHoraEntregaReal().isAfter(e.getHoraEstimada())) {
                cumplimiento = "CON RETRASO";
            } else {
                cumplimiento = "A TIEMPO";
            }
        } else if (e.getIdEstadoEntrega() != null && e.getIdEstadoEntrega() == 4) { // Cancelado/Fallido
            cumplimiento = "NO ENTREGADO";
        }

        return ReporteEntregaItemDTO.builder()
                .idEntrega(e.getIdEntrega())
                .idPedido(e.getIdPedido())
                .cliente(e.getCliente())
                .direccion(e.getDireccion())
                .estadoEntrega(e.getEstadoEntrega())
                .horaEstimada(estimadaStr)
                .horaEntregaReal(realStr)
                .cumplimiento(cumplimiento)
                .notas(e.getNotas())
                .build();
    }
}
