package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.ReporteEntregaDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteEntregaItemDTO;
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
public class EntregaPdfGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COLOR_CABECERA = new Color(33, 64, 115);
    private static final Color COLOR_FILA_ALTERNA = new Color(242, 245, 250);
    private static final Color COLOR_VERDE = new Color(22, 101, 52);
    private static final Color COLOR_ROJO = new Color(153, 27, 27);

    private static final Font FUENTE_EMPRESA = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_CABECERA);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FUENTE_ETIQUETA = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FUENTE_EXITO = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_VERDE);
    private static final Font FUENTE_ALERTA = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_ROJO);
    private static final Font FUENTE_TABLA_CABECERA = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);

    private static final String[] COLUMNAS = {"ID Entrega", "Pedido", "Cliente", "Dirección", "H. Estimada", "H. Real", "Estado", "Cumplimiento"};
    private static final float[] ANCHOS = {1.3f, 1.2f, 3.2f, 3.5f, 1.8f, 1.8f, 1.6f, 1.8f};

    private final String razonSocialEmpresa;

    public EntregaPdfGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReporteEntregaDTO reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 36);
        try {
            PdfWriter.getInstance(documento, salida);

            HeaderFooter pie = new HeaderFooter(
                    new Phrase("Reporte de Entregas y Cumplimiento - Página ", FUENTE_PIE),
                    new Phrase(" | " + razonSocialEmpresa, FUENTE_PIE)
            );
            pie.setBorder(Rectangle.NO_BORDER);
            pie.setAlignment(Element.ALIGN_RIGHT);
            documento.setFooter(pie);

            documento.open();

            // Encabezado
            Paragraph pEmpresa = new Paragraph(razonSocialEmpresa, FUENTE_EMPRESA);
            pEmpresa.setSpacingAfter(2);
            documento.add(pEmpresa);

            Paragraph pTitulo = new Paragraph("REPORTE DE ENTREGAS Y CUMPLIMIENTO", FUENTE_TITULO);
            pTitulo.setSpacingAfter(10);
            documento.add(pTitulo);

            // Metadatos
            PdfPTable tablaMeta = new PdfPTable(4);
            tablaMeta.setWidthPercentage(100);
            tablaMeta.setSpacingAfter(12);

            agregarMetadato(tablaMeta, "Generado:", reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_FECHA));
            agregarMetadato(tablaMeta, "Total Entregas:", String.valueOf(reporte.getTotalEntregas()));
            agregarMetadato(tablaMeta, "A Tiempo:", String.valueOf(reporte.getTotalATiempo()));
            agregarMetadato(tablaMeta, "% Cumplimiento:", reporte.getPorcentajeCumplimiento() + " %");
            documento.add(tablaMeta);

            // Tabla de datos
            PdfPTable tabla = new PdfPTable(ANCHOS);
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);

            for (String col : COLUMNAS) {
                PdfPCell celda = new PdfPCell(new Phrase(col, FUENTE_TABLA_CABECERA));
                celda.setBackgroundColor(COLOR_CABECERA);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            boolean alterna = false;
            for (ReporteEntregaItemDTO item : reporte.getItems()) {
                Color fondo = alterna ? COLOR_FILA_ALTERNA : Color.WHITE;

                tabla.addCell(celdaDato("#" + item.getIdEntrega(), Element.ALIGN_CENTER, fondo, FUENTE_NORMAL));
                tabla.addCell(celdaDato("#" + item.getIdPedido(), Element.ALIGN_CENTER, fondo, FUENTE_NORMAL));
                tabla.addCell(celdaDato(item.getCliente(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
                tabla.addCell(celdaDato(item.getDireccion(), Element.ALIGN_LEFT, fondo, FUENTE_NORMAL));
                tabla.addCell(celdaDato(item.getHoraEstimada(), Element.ALIGN_CENTER, fondo, FUENTE_NORMAL));
                tabla.addCell(celdaDato(item.getHoraEntregaReal(), Element.ALIGN_CENTER, fondo, FUENTE_NORMAL));
                tabla.addCell(celdaDato(item.getEstadoEntrega(), Element.ALIGN_CENTER, fondo, FUENTE_NORMAL));

                Font fuenteCumplimiento = "A TIEMPO".equals(item.getCumplimiento()) ? FUENTE_EXITO
                        : "CON RETRASO".equals(item.getCumplimiento()) ? FUENTE_ALERTA : FUENTE_NORMAL;
                tabla.addCell(celdaDato(item.getCumplimiento(), Element.ALIGN_CENTER, fondo, fuenteCumplimiento));

                alterna = !alterna;
            }

            documento.add(tabla);
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte PDF de entregas", e);
        }
    }

    private void agregarMetadato(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell cEtiqueta = new PdfPCell(new Phrase(etiqueta, FUENTE_ETIQUETA));
        cEtiqueta.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cEtiqueta);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "-", FUENTE_NORMAL));
        cValor.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cValor);
    }

    private PdfPCell celdaDato(String texto, int alineacion, Color fondo, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "-", fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setBackgroundColor(fondo);
        celda.setPadding(4);
        return celda;
    }
}
