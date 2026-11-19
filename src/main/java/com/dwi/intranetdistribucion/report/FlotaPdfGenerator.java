package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.DetalleRutaProductividadDTO;
import com.dwi.intranetdistribucion.dto.response.ProductividadConductorDTO;
import com.dwi.intranetdistribucion.dto.response.ProductividadVehiculoDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteProductividadFlotaDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class FlotaPdfGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COLOR_CABECERA = new Color(33, 64, 115);
    private static final Color COLOR_FILA_ALTERNA = new Color(242, 245, 250);
    private static final Color COLOR_VERDE = new Color(22, 101, 52);

    private static final Font FUENTE_EMPRESA = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_CABECERA);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FUENTE_SUBTITULO = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_CABECERA);
    private static final Font FUENTE_ETIQUETA = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FUENTE_EXITO = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_VERDE);
    private static final Font FUENTE_TABLA_CABECERA = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);

    private final String razonSocialEmpresa;

    public FlotaPdfGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora Backus}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReporteProductividadFlotaDTO reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 36);
        try {
            PdfWriter.getInstance(documento, salida);

            HeaderFooter pie = new HeaderFooter(
                    new Phrase("Reporte de Productividad de Flota - Página ", FUENTE_PIE),
                    new Phrase(" | " + razonSocialEmpresa, FUENTE_PIE)
            );
            pie.setBorder(Rectangle.NO_BORDER);
            pie.setAlignment(Element.ALIGN_RIGHT);
            documento.setFooter(pie);

            documento.open();

            // Encabezado institucional
            Paragraph pEmpresa = new Paragraph(razonSocialEmpresa, FUENTE_EMPRESA);
            pEmpresa.setSpacingAfter(2);
            documento.add(pEmpresa);

            Paragraph pTitulo = new Paragraph("REPORTE DE PRODUCTIVIDAD DE FLOTA Y CONDUCTORES", FUENTE_TITULO);
            pTitulo.setSpacingAfter(10);
            documento.add(pTitulo);

            // Resumen de Métricas Clave
            PdfPTable tKpis = new PdfPTable(4);
            tKpis.setWidthPercentage(100);
            tKpis.setSpacingAfter(12);

            agregarCeldaKpi(tKpis, "Total Rutas Programadas", String.valueOf(reporte.getTotalRutasProgramadas()));
            agregarCeldaKpi(tKpis, "Rutas Completadas", String.valueOf(reporte.getTotalRutasCompletadas()));
            agregarCeldaKpi(tKpis, "Tasa Cumplimiento", String.format("%.1f%%", reporte.getTasaCumplimientoGeneralPct()));
            agregarCeldaKpi(tKpis, "Tiempo Promedio por Ruta", reporte.getTiempoPromedioGeneralFormateado());
            documento.add(tKpis);

            // 1. PRODUCTIVIDAD POR VEHÍCULO
            Paragraph pVehiculos = new Paragraph("1. Productividad por Vehículo", FUENTE_SUBTITULO);
            pVehiculos.setSpacingAfter(6);
            documento.add(pVehiculos);

            PdfPTable tVehiculos = new PdfPTable(7);
            tVehiculos.setWidthPercentage(100);
            tVehiculos.setWidths(new float[]{1.5f, 2.5f, 2.0f, 1.5f, 1.5f, 2.0f, 1.5f});
            tVehiculos.setSpacingAfter(12);

            String[] colVeh = {"Placa", "Modelo / Marca", "Capacidad", "Asignadas", "Completadas", "Tiempo Promedio", "Cumplimiento"};
            for (String c : colVeh) {
                PdfPCell th = new PdfPCell(new Phrase(c, FUENTE_TABLA_CABECERA));
                th.setBackgroundColor(COLOR_CABECERA);
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                th.setPadding(5);
                tVehiculos.addCell(th);
            }

            boolean alt = false;
            for (ProductividadVehiculoDTO v : reporte.getMetricasVehiculos()) {
                Color fondo = alt ? COLOR_FILA_ALTERNA : Color.WHITE;
                alt = !alt;

                agregarCelda(tVehiculos, v.getPlaca(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tVehiculos, v.getMarca() + " " + v.getModelo(), Element.ALIGN_LEFT, fondo);
                agregarCelda(tVehiculos, String.format("%.0f kg", v.getCapacidadKg()), Element.ALIGN_RIGHT, fondo);
                agregarCelda(tVehiculos, String.valueOf(v.getRutasAsignadas()), Element.ALIGN_CENTER, fondo);
                agregarCelda(tVehiculos, String.valueOf(v.getRutasCompletadas()), Element.ALIGN_CENTER, fondo);
                agregarCelda(tVehiculos, v.getTiempoPromedioFormateado(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tVehiculos, String.format("%.1f%%", v.getTasaCumplimientoPct()), Element.ALIGN_CENTER, fondo);
            }
            documento.add(tVehiculos);

            // 2. PRODUCTIVIDAD POR CONDUCTOR
            Paragraph pConductores = new Paragraph("2. Productividad por Conductor", FUENTE_SUBTITULO);
            pConductores.setSpacingAfter(6);
            documento.add(pConductores);

            PdfPTable tConductores = new PdfPTable(6);
            tConductores.setWidthPercentage(100);
            tConductores.setWidths(new float[]{3.0f, 1.5f, 1.8f, 1.5f, 2.0f, 1.5f});
            tConductores.setSpacingAfter(12);

            String[] colCond = {"Conductor", "DNI", "Licencia", "Completadas", "Tiempo Promedio", "Cumplimiento"};
            for (String c : colCond) {
                PdfPCell th = new PdfPCell(new Phrase(c, FUENTE_TABLA_CABECERA));
                th.setBackgroundColor(COLOR_CABECERA);
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                th.setPadding(5);
                tConductores.addCell(th);
            }

            alt = false;
            for (ProductividadConductorDTO c : reporte.getMetricasConductores()) {
                Color fondo = alt ? COLOR_FILA_ALTERNA : Color.WHITE;
                alt = !alt;

                agregarCelda(tConductores, c.getNombreCompleto(), Element.ALIGN_LEFT, fondo);
                agregarCelda(tConductores, c.getDni(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tConductores, c.getNroLicencia(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tConductores, String.valueOf(c.getRutasCompletadas()), Element.ALIGN_CENTER, fondo);
                agregarCelda(tConductores, c.getTiempoPromedioFormateado(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tConductores, String.format("%.1f%%", c.getTasaCumplimientoPct()), Element.ALIGN_CENTER, fondo);
            }
            documento.add(tConductores);

            // 3. REGISTRO DE RUTAS REALIZADAS
            Paragraph pRutas = new Paragraph("3. Historial de Rutas y Tiempos de Ejecución", FUENTE_SUBTITULO);
            pRutas.setSpacingAfter(6);
            documento.add(pRutas);

            PdfPTable tDetalles = new PdfPTable(6);
            tDetalles.setWidthPercentage(100);
            tDetalles.setWidths(new float[]{1.2f, 2.8f, 1.8f, 2.5f, 1.8f, 1.8f});

            String[] colDet = {"ID Prog", "Ruta", "Vehículo", "Conductor", "Duración", "Estado"};
            for (String c : colDet) {
                PdfPCell th = new PdfPCell(new Phrase(c, FUENTE_TABLA_CABECERA));
                th.setBackgroundColor(COLOR_CABECERA);
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                th.setPadding(5);
                tDetalles.addCell(th);
            }

            alt = false;
            for (DetalleRutaProductividadDTO d : reporte.getDetallesRutas()) {
                Color fondo = alt ? COLOR_FILA_ALTERNA : Color.WHITE;
                alt = !alt;

                agregarCelda(tDetalles, "#" + d.getIdProgramacion(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tDetalles, d.getRuta(), Element.ALIGN_LEFT, fondo);
                agregarCelda(tDetalles, d.getVehiculoPlaca(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tDetalles, d.getConductorNombre(), Element.ALIGN_LEFT, fondo);
                agregarCelda(tDetalles, d.getDuracionFormateada(), Element.ALIGN_CENTER, fondo);
                agregarCelda(tDetalles, d.getEstadoProgramacion(), Element.ALIGN_CENTER, fondo);
            }
            documento.add(tDetalles);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de productividad de flota", e);
        }
    }

    private void agregarCeldaKpi(PdfPTable tabla, String titulo, String valor) {
        PdfPCell celda = new PdfPCell();
        celda.setPadding(6);
        celda.setBackgroundColor(COLOR_FILA_ALTERNA);

        Paragraph pTit = new Paragraph(titulo.toUpperCase(), FUENTE_ETIQUETA);
        pTit.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(pTit);

        Paragraph pVal = new Paragraph(valor, FUENTE_TITULO);
        pVal.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(pVal);

        tabla.addCell(celda);
    }

    private void agregarCelda(PdfPTable tabla, String texto, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "-", FUENTE_NORMAL));
        celda.setHorizontalAlignment(alineacion);
        celda.setBackgroundColor(fondo);
        celda.setPadding(4);
        tabla.addCell(celda);
    }
}
