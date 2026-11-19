package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.response.*;
import com.dwi.intranetdistribucion.report.FlotaExcelGenerator;
import com.dwi.intranetdistribucion.report.FlotaPdfGenerator;
import com.dwi.intranetdistribucion.repository.ProgramacionRutaRepository;
import com.dwi.intranetdistribucion.repository.projection.ProgramacionReporteView;
import com.dwi.intranetdistribucion.service.ReporteFlotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteFlotaServiceImpl implements ReporteFlotaService {

    private static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ProgramacionRutaRepository programacionRutaRepository;
    private final FlotaPdfGenerator flotaPdfGenerator;
    private final FlotaExcelGenerator flotaExcelGenerator;

    @Override
    @Transactional(readOnly = true)
    public ReporteProductividadFlotaDTO obtenerReporte(LocalDate fechaInicio, LocalDate fechaFin, Integer idVehiculo, Integer idConductor) {
        List<ProgramacionReporteView> views = programacionRutaRepository.obtenerReporteProductividad(
                fechaInicio, fechaFin, idVehiculo, idConductor);

        List<DetalleRutaProductividadDTO> detalles = new ArrayList<>();
        Map<Integer, List<DetalleRutaProductividadDTO>> porVehiculo = new HashMap<>();
        Map<Integer, List<DetalleRutaProductividadDTO>> porConductor = new HashMap<>();

        Map<Integer, ProgramacionReporteView> infoVehiculoMap = new HashMap<>();
        Map<Integer, ProgramacionReporteView> infoConductorMap = new HashMap<>();

        long sumaMinutosGeneral = 0;
        int totalCompletadas = 0;

        for (ProgramacionReporteView v : views) {
            long duracionMin = calcularDuracionMinutos(v);
            boolean completada = (v.getIdEstadoProgramacion() != null && v.getIdEstadoProgramacion() == 3)
                    || (v.getFinReal() != null);

            if (completada) {
                totalCompletadas++;
                sumaMinutosGeneral += duracionMin;
            }

            DetalleRutaProductividadDTO detalle = DetalleRutaProductividadDTO.builder()
                    .idProgramacion(v.getIdProgramacion())
                    .ruta(v.getRuta())
                    .vehiculoPlaca(v.getVehiculoPlaca())
                    .conductorNombre(v.getConductorNombres() + " " + v.getConductorApellidos())
                    .inicioProgramado(v.getInicioProgramado())
                    .finProgramado(v.getFinProgramado())
                    .inicioReal(v.getInicioReal())
                    .finReal(v.getFinReal())
                    .duracionMinutos(duracionMin)
                    .duracionFormateada(formatearMinutos(duracionMin))
                    .idEstadoProgramacion(v.getIdEstadoProgramacion())
                    .estadoProgramacion(v.getEstadoProgramacion() != null ? v.getEstadoProgramacion() : "Desconocido")
                    .completada(completada)
                    .build();

            detalles.add(detalle);

            if (v.getIdVehiculo() != null) {
                porVehiculo.computeIfAbsent(v.getIdVehiculo(), k -> new ArrayList<>()).add(detalle);
                infoVehiculoMap.putIfAbsent(v.getIdVehiculo(), v);
            }
            if (v.getIdConductor() != null) {
                porConductor.computeIfAbsent(v.getIdConductor(), k -> new ArrayList<>()).add(detalle);
                infoConductorMap.putIfAbsent(v.getIdConductor(), v);
            }
        }

        // Métricas de Vehículos
        List<ProductividadVehiculoDTO> metricasVehiculos = new ArrayList<>();
        String vehiculoTop = "-";
        int maxRutasVeh = -1;

        for (Map.Entry<Integer, List<DetalleRutaProductividadDTO>> entry : porVehiculo.entrySet()) {
            Integer idV = entry.getKey();
            List<DetalleRutaProductividadDTO> lista = entry.getValue();
            ProgramacionReporteView info = infoVehiculoMap.get(idV);

            int asignadas = lista.size();
            long completadas = lista.stream().filter(DetalleRutaProductividadDTO::getCompletada).count();
            long minutosTotales = lista.stream()
                    .filter(DetalleRutaProductividadDTO::getCompletada)
                    .mapToLong(DetalleRutaProductividadDTO::getDuracionMinutos)
                    .sum();

            double promedioMin = completadas > 0 ? (double) minutosTotales / completadas : 0.0;
            double tasaCumplimiento = asignadas > 0 ? ((double) completadas / asignadas) * 100.0 : 0.0;

            if (completadas > maxRutasVeh) {
                maxRutasVeh = (int) completadas;
                vehiculoTop = info.getVehiculoPlaca() + " (" + info.getVehiculoMarca() + ")";
            }

            metricasVehiculos.add(ProductividadVehiculoDTO.builder()
                    .idVehiculo(idV)
                    .placa(info.getVehiculoPlaca())
                    .marca(info.getVehiculoMarca())
                    .modelo(info.getVehiculoModelo())
                    .capacidadKg(info.getVehiculoCapacidadKg())
                    .rutasAsignadas(asignadas)
                    .rutasCompletadas((int) completadas)
                    .tiempoTotalMinutos(minutosTotales)
                    .tiempoPromedioMinutos(promedioMin)
                    .tiempoPromedioFormateado(formatearMinutos((long) promedioMin))
                    .tasaCumplimientoPct(Math.round(tasaCumplimiento * 10.0) / 10.0)
                    .build());
        }

        // Métricas de Conductores
        List<ProductividadConductorDTO> metricasConductores = new ArrayList<>();
        String conductorTop = "-";
        int maxRutasCond = -1;

        for (Map.Entry<Integer, List<DetalleRutaProductividadDTO>> entry : porConductor.entrySet()) {
            Integer idC = entry.getKey();
            List<DetalleRutaProductividadDTO> lista = entry.getValue();
            ProgramacionReporteView info = infoConductorMap.get(idC);

            int asignadas = lista.size();
            long completadas = lista.stream().filter(DetalleRutaProductividadDTO::getCompletada).count();
            long minutosTotales = lista.stream()
                    .filter(DetalleRutaProductividadDTO::getCompletada)
                    .mapToLong(DetalleRutaProductividadDTO::getDuracionMinutos)
                    .sum();

            double promedioMin = completadas > 0 ? (double) minutosTotales / completadas : 0.0;
            double tasaCumplimiento = asignadas > 0 ? ((double) completadas / asignadas) * 100.0 : 0.0;

            if (completadas > maxRutasCond) {
                maxRutasCond = (int) completadas;
                conductorTop = info.getConductorNombres() + " " + info.getConductorApellidos();
            }

            metricasConductores.add(ProductividadConductorDTO.builder()
                    .idConductor(idC)
                    .nombres(info.getConductorNombres())
                    .apellidos(info.getConductorApellidos())
                    .nombreCompleto(info.getConductorNombres() + " " + info.getConductorApellidos())
                    .dni(info.getConductorDni())
                    .nroLicencia(info.getConductorLicencia())
                    .rutasAsignadas(asignadas)
                    .rutasCompletadas((int) completadas)
                    .tiempoTotalMinutos(minutosTotales)
                    .tiempoPromedioMinutos(promedioMin)
                    .tiempoPromedioFormateado(formatearMinutos((long) promedioMin))
                    .tasaCumplimientoPct(Math.round(tasaCumplimiento * 10.0) / 10.0)
                    .build());
        }

        int totalProgramadas = views.size();
        double tasaGeneral = totalProgramadas > 0 ? ((double) totalCompletadas / totalProgramadas) * 100.0 : 0.0;
        double promedioGeneral = totalCompletadas > 0 ? (double) sumaMinutosGeneral / totalCompletadas : 0.0;

        return ReporteProductividadFlotaDTO.builder()
                .totalRutasProgramadas(totalProgramadas)
                .totalRutasCompletadas(totalCompletadas)
                .tasaCumplimientoGeneralPct(Math.round(tasaGeneral * 10.0) / 10.0)
                .tiempoPromedioGeneralMinutos(promedioGeneral)
                .tiempoPromedioGeneralFormateado(formatearMinutos((long) promedioGeneral))
                .vehiculoMasProductivo(vehiculoTop)
                .conductorMasProductivo(conductorTop)
                .metricasVehiculos(metricasVehiculos)
                .metricasConductores(metricasConductores)
                .detallesRutas(detalles)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarPdf(LocalDate fechaInicio, LocalDate fechaFin, Integer idVehiculo, Integer idConductor) {
        ReporteProductividadFlotaDTO datos = obtenerReporte(fechaInicio, fechaFin, idVehiculo, idConductor);
        byte[] contenido = flotaPdfGenerator.generar(datos);
        String nombre = "reporte-productividad-flota-" + LocalDate.now().format(FORMATO_FECHA_ARCHIVO) + ".pdf";
        return new ArchivoReporteDTO(nombre, contenido);
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoReporteDTO generarExcel(LocalDate fechaInicio, LocalDate fechaFin, Integer idVehiculo, Integer idConductor) {
        ReporteProductividadFlotaDTO datos = obtenerReporte(fechaInicio, fechaFin, idVehiculo, idConductor);
        byte[] contenido = flotaExcelGenerator.generar(datos);
        String nombre = "reporte-productividad-flota-" + LocalDate.now().format(FORMATO_FECHA_ARCHIVO) + ".xlsx";
        return new ArchivoReporteDTO(nombre, contenido);
    }

    private long calcularDuracionMinutos(ProgramacionReporteView v) {
        if (v.getInicioReal() != null && v.getFinReal() != null) {
            return Math.max(0, Duration.between(v.getInicioReal(), v.getFinReal()).toMinutes());
        }
        if (v.getInicioProgramado() != null && v.getFinProgramado() != null) {
            return Math.max(0, Duration.between(v.getInicioProgramado(), v.getFinProgramado()).toMinutes());
        }
        return 0;
    }

    private String formatearMinutos(long minutos) {
        if (minutos <= 0) return "0 min";
        long horas = minutos / 60;
        long mins = minutos % 60;
        if (horas > 0) {
            return horas + "h " + mins + "m";
        }
        return mins + " min";
    }
}
