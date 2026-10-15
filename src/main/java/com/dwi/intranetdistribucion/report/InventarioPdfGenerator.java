package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.InventarioReporteDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteInventarioDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Genera el reporte de inventario en PDF (A4 horizontal)
@Component
public class InventarioPdfGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COLOR_CABECERA = new Color(33, 64, 115);
    private static final Color COLOR_FILA_ALTERNA = new Color(242, 245, 250);
    private static final Color COLOR_ALERTA = new Color(192, 0, 0);
    private static final Color COLOR_FONDO_ALERTA = new Color(253, 233, 233);

    private static final Font FUENTE_EMPRESA = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_CABECERA);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FUENTE_ETIQUETA = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FUENTE_ALERTA = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_ALERTA);
    private static final Font FUENTE_TABLA_CABECERA = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);

    private static final String[] COLUMNAS = {
            "Almacén", "Sede", "Código", "Producto", "Categoría", "Unidad", "Stock actual", "Stock mínimo", "Estado"};
    private static final float[] ANCHOS = {2.2f, 1.8f, 1.2f, 3.6f, 2f, 0.9f, 1.3f, 1.3f, 1.2f};

    private final String razonSocialEmpresa;

    public InventarioPdfGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReporteInventarioDTO reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            HeaderFooter pie = new HeaderFooter(new Phrase("Reporte de inventario - Página ", FUENTE_PIE), true);
            pie.setBorder(Rectangle.NO_BORDER);
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.setFooter(pie);
            documento.open();

            documento.add(new Paragraph(razonSocialEmpresa, FUENTE_EMPRESA));
            documento.add(new Paragraph("Reporte de inventario actual", FUENTE_TITULO));
            documento.add(new Paragraph(" ", FUENTE_NORMAL));
            documento.add(construirResumen(reporte));
            documento.add(new Paragraph(" ", FUENTE_NORMAL));
            documento.add(construirTabla(reporte));

            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte de inventario", e);
        }
        return salida.toByteArray();
    }

    private PdfPTable construirResumen(ReporteInventarioDTO reporte) {
        PdfPTable tabla = new PdfPTable(new float[]{1.4f, 3f, 1.4f, 3f});
        tabla.setWidthPercentage(70);
        tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
        agregarDato(tabla, "Generado", reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_FECHA));
        agregarDato(tabla, "Total de registros", String.valueOf(reporte.getTotalRegistros()));
        agregarDato(tabla, "Almacén", reporte.getAlmacenFiltro());
        agregarDato(tabla, "Con stock bajo", String.valueOf(reporte.getTotalBajoStock()));
        agregarDato(tabla, "Categoría", reporte.getCategoriaFiltro());
        agregarDato(tabla, "", "");
        return tabla;
    }

    private PdfPTable construirTabla(ReporteInventarioDTO reporte) {
        PdfPTable tabla = new PdfPTable(ANCHOS);
        tabla.setWidthPercentage(100);
        for (String columna : COLUMNAS) {
            PdfPCell cabecera = new PdfPCell(new Phrase(columna, FUENTE_TABLA_CABECERA));
            cabecera.setBackgroundColor(COLOR_CABECERA);
            cabecera.setHorizontalAlignment(Element.ALIGN_CENTER);
            cabecera.setPadding(5);
            tabla.addCell(cabecera);
        }
        tabla.setHeaderRows(1);

        if (reporte.getItems().isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase(
                    "No hay registros de inventario para los filtros seleccionados.", FUENTE_NORMAL));
            vacia.setColspan(COLUMNAS.length);
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setPadding(8);
            tabla.addCell(vacia);
            return tabla;
        }

        int fila = 0;
        for (InventarioReporteDTO item : reporte.getItems()) {
            boolean bajo = Boolean.TRUE.equals(item.getBajoStock());
            Color fondo = bajo ? COLOR_FONDO_ALERTA : (fila++ % 2 == 0 ? null : COLOR_FILA_ALTERNA);
            tabla.addCell(celda(item.getAlmacen(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(item.getSede(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(item.getCodigo(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(item.getProducto(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(item.getCategoria(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(item.getUnidad(), Element.ALIGN_CENTER, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(numero(item.getStockActual()), Element.ALIGN_RIGHT, fondo, bajo ? FUENTE_ALERTA : FUENTE_NORMAL));
            tabla.addCell(celda(numero(item.getStockMinimo()), Element.ALIGN_RIGHT, fondo, FUENTE_NORMAL));
            tabla.addCell(celda(bajo ? "Stock bajo" : "OK", Element.ALIGN_CENTER, fondo, bajo ? FUENTE_ALERTA : FUENTE_NORMAL));
        }
        return tabla;
    }

    private void agregarDato(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FUENTE_ETIQUETA));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setPadding(2);
        tabla.addCell(celdaEtiqueta);
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, FUENTE_NORMAL));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPadding(2);
        tabla.addCell(celdaValor);
    }

    private PdfPCell celda(String texto, int alineacion, Color fondo, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private String numero(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor);
    }
}
