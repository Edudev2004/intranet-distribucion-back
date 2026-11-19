package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.DetalleRutaProductividadDTO;
import com.dwi.intranetdistribucion.dto.response.ProductividadConductorDTO;
import com.dwi.intranetdistribucion.dto.response.ProductividadVehiculoDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteProductividadFlotaDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class FlotaExcelGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final XSSFColor COLOR_CABECERA = new XSSFColor(new byte[]{(byte) 33, (byte) 64, (byte) 115}, null);
    private static final XSSFColor COLOR_BLANCO = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);

    private final String razonSocialEmpresa;

    public FlotaExcelGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora Backus}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReporteProductividadFlotaDTO reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);

            // =========================================================================
            // HOJA 1: PRODUCTIVIDAD VEHÍCULOS
            // =========================================================================
            Sheet hVehiculos = libro.createSheet("Vehículos");
            Row rTit1 = hVehiculos.createRow(0);
            crearCelda(rTit1, 0, razonSocialEmpresa + " - Reporte de Productividad de Vehículos", estilos.titulo);
            hVehiculos.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            crearCelda(hVehiculos.createRow(1), 0, "Rutas programadas: " + reporte.getTotalRutasProgramadas()
                    + " | Rutas completadas: " + reporte.getTotalRutasCompletadas()
                    + " | Cumplimiento general: " + String.format("%.1f%%", reporte.getTasaCumplimientoGeneralPct()), estilos.info);
            crearCelda(hVehiculos.createRow(2), 0, "Tiempo promedio por ruta: " + reporte.getTiempoPromedioGeneralFormateado()
                    + " | Vehículo más productivo: " + reporte.getVehiculoMasProductivo(), estilos.info);

            String[] colVeh = {"ID Vehículo", "Placa", "Marca / Modelo", "Capacidad (kg)", "Rutas Asignadas", "Rutas Completadas", "Tiempo Promedio", "% Cumplimiento"};
            int[] anchVeh = {14, 16, 25, 18, 18, 18, 20, 18};

            Row cabVeh = hVehiculos.createRow(4);
            for (int i = 0; i < colVeh.length; i++) {
                crearCelda(cabVeh, i, colVeh[i], estilos.cabecera);
            }

            int fVeh = 5;
            for (ProductividadVehiculoDTO v : reporte.getMetricasVehiculos()) {
                Row fila = hVehiculos.createRow(fVeh++);
                crearCelda(fila, 0, v.getIdVehiculo(), estilos.datoCentro);
                crearCelda(fila, 1, v.getPlaca(), estilos.datoCentro);
                crearCelda(fila, 2, v.getMarca() + " " + v.getModelo(), estilos.datoTexto);
                crearCelda(fila, 3, v.getCapacidadKg() != null ? v.getCapacidadKg().doubleValue() : 0.0, estilos.datoNumero);
                crearCelda(fila, 4, v.getRutasAsignadas(), estilos.datoCentro);
                crearCelda(fila, 5, v.getRutasCompletadas(), estilos.datoCentro);
                crearCelda(fila, 6, v.getTiempoPromedioFormateado(), estilos.datoCentro);
                crearCelda(fila, 7, String.format("%.1f%%", v.getTasaCumplimientoPct()), estilos.datoCentro);
            }

            for (int i = 0; i < anchVeh.length; i++) {
                hVehiculos.setColumnWidth(i, anchVeh[i] * 256);
            }

            // =========================================================================
            // HOJA 2: PRODUCTIVIDAD CONDUCTORES
            // =========================================================================
            Sheet hConductores = libro.createSheet("Conductores");
            Row rTit2 = hConductores.createRow(0);
            crearCelda(rTit2, 0, razonSocialEmpresa + " - Reporte de Productividad de Conductores", estilos.titulo);
            hConductores.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            crearCelda(hConductores.createRow(1), 0, "Conductor más productivo: " + reporte.getConductorMasProductivo(), estilos.info);

            String[] colCond = {"ID Conductor", "Nombre Completo", "DNI", "Licencia", "Rutas Asignadas", "Rutas Completadas", "Tiempo Promedio", "% Cumplimiento"};
            int[] anchCond = {14, 30, 16, 18, 18, 18, 20, 18};

            Row cabCond = hConductores.createRow(3);
            for (int i = 0; i < colCond.length; i++) {
                crearCelda(cabCond, i, colCond[i], estilos.cabecera);
            }

            int fCond = 4;
            for (ProductividadConductorDTO c : reporte.getMetricasConductores()) {
                Row fila = hConductores.createRow(fCond++);
                crearCelda(fila, 0, c.getIdConductor(), estilos.datoCentro);
                crearCelda(fila, 1, c.getNombreCompleto(), estilos.datoTexto);
                crearCelda(fila, 2, c.getDni(), estilos.datoCentro);
                crearCelda(fila, 3, c.getNroLicencia(), estilos.datoCentro);
                crearCelda(fila, 4, c.getRutasAsignadas(), estilos.datoCentro);
                crearCelda(fila, 5, c.getRutasCompletadas(), estilos.datoCentro);
                crearCelda(fila, 6, c.getTiempoPromedioFormateado(), estilos.datoCentro);
                crearCelda(fila, 7, String.format("%.1f%%", c.getTasaCumplimientoPct()), estilos.datoCentro);
            }

            for (int i = 0; i < anchCond.length; i++) {
                hConductores.setColumnWidth(i, anchCond[i] * 256);
            }

            // =========================================================================
            // HOJA 3: DETALLE DE RUTAS
            // =========================================================================
            Sheet hDetalles = libro.createSheet("Detalle de Rutas");
            Row rTit3 = hDetalles.createRow(0);
            crearCelda(rTit3, 0, razonSocialEmpresa + " - Historial de Rutas y Tiempos de Ejecución", estilos.titulo);
            hDetalles.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            String[] colDet = {"ID Prog", "Ruta", "Vehículo Placa", "Conductor", "Duración", "Estado", "¿Completada?"};
            int[] anchDet = {12, 28, 16, 28, 18, 18, 15};

            Row cabDet = hDetalles.createRow(2);
            for (int i = 0; i < colDet.length; i++) {
                crearCelda(cabDet, i, colDet[i], estilos.cabecera);
            }

            int fDet = 3;
            for (DetalleRutaProductividadDTO d : reporte.getDetallesRutas()) {
                Row fila = hDetalles.createRow(fDet++);
                crearCelda(fila, 0, d.getIdProgramacion(), estilos.datoCentro);
                crearCelda(fila, 1, d.getRuta(), estilos.datoTexto);
                crearCelda(fila, 2, d.getVehiculoPlaca(), estilos.datoCentro);
                crearCelda(fila, 3, d.getConductorNombre(), estilos.datoTexto);
                crearCelda(fila, 4, d.getDuracionFormateada(), estilos.datoCentro);
                crearCelda(fila, 5, d.getEstadoProgramacion(), estilos.datoCentro);
                crearCelda(fila, 6, Boolean.TRUE.equals(d.getCompletada()) ? "SÍ" : "NO", estilos.datoCentro);
            }

            for (int i = 0; i < anchDet.length; i++) {
                hDetalles.setColumnWidth(i, anchDet[i] * 256);
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el Excel de productividad de flota", e);
        }
    }

    private void crearCelda(Row fila, int col, Object valor, CellStyle estilo) {
        Cell celda = fila.createCell(col);
        if (valor == null) {
            celda.setCellValue("-");
        } else if (valor instanceof Number) {
            celda.setCellValue(((Number) valor).doubleValue());
        } else {
            celda.setCellValue(valor.toString());
        }
        celda.setCellStyle(estilo);
    }

    private static class Estilos {
        final CellStyle titulo;
        final CellStyle info;
        final CellStyle cabecera;
        final CellStyle datoCentro;
        final CellStyle datoTexto;
        final CellStyle datoNumero;

        Estilos(XSSFWorkbook libro) {
            XSSFFont fuenteTit = libro.createFont();
            fuenteTit.setFontHeightInPoints((short) 13);
            fuenteTit.setBold(true);
            titulo = libro.createCellStyle();
            titulo.setFont(fuenteTit);

            XSSFFont fuenteInfo = libro.createFont();
            fuenteInfo.setFontHeightInPoints((short) 9);
            fuenteInfo.setItalic(true);
            info = libro.createCellStyle();
            info.setFont(fuenteInfo);

            XSSFFont fuenteCab = libro.createFont();
            fuenteCab.setFontHeightInPoints((short) 9);
            fuenteCab.setBold(true);
            fuenteCab.setColor(COLOR_BLANCO);

            cabecera = libro.createCellStyle();
            ((XSSFCellStyle) cabecera).setFillForegroundColor(COLOR_CABECERA);
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setFont(fuenteCab);
            cabecera.setAlignment(HorizontalAlignment.CENTER);
            cabecera.setVerticalAlignment(VerticalAlignment.CENTER);
            cabecera.setBorderBottom(BorderStyle.THIN);

            datoCentro = libro.createCellStyle();
            datoCentro.setAlignment(HorizontalAlignment.CENTER);
            datoCentro.setBorderBottom(BorderStyle.THIN);
            datoCentro.setBorderTop(BorderStyle.THIN);
            datoCentro.setBorderLeft(BorderStyle.THIN);
            datoCentro.setBorderRight(BorderStyle.THIN);

            datoTexto = libro.createCellStyle();
            datoTexto.setAlignment(HorizontalAlignment.LEFT);
            datoTexto.setBorderBottom(BorderStyle.THIN);
            datoTexto.setBorderTop(BorderStyle.THIN);
            datoTexto.setBorderLeft(BorderStyle.THIN);
            datoTexto.setBorderRight(BorderStyle.THIN);

            datoNumero = libro.createCellStyle();
            datoNumero.setAlignment(HorizontalAlignment.RIGHT);
            datoNumero.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));
            datoNumero.setBorderBottom(BorderStyle.THIN);
            datoNumero.setBorderTop(BorderStyle.THIN);
            datoNumero.setBorderLeft(BorderStyle.THIN);
            datoNumero.setBorderRight(BorderStyle.THIN);
        }
    }
}
