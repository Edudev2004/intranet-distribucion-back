package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.ReportePedidoDTO;
import com.dwi.intranetdistribucion.dto.response.ReportePedidoItemDTO;
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
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class PedidoPdfGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COLOR_CABECERA = new Color(33, 64, 115);
    private static final Color COLOR_FILA_ALTERNA = new Color(242, 245, 250);

    private static final Font FUENTE_EMPRESA = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_CABECERA);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FUENTE_ETIQUETA = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FUENTE_TABLA_CABECERA = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);

    private static final String[] COLUMNAS = {"N° Pedido", "Fecha", "Cliente", "Almacén", "Estado", "Total (S/)"};
    private static final float[] ANCHOS = {1.5f, 2.2f, 4.0f, 2.5f, 2.0f, 1.8f};

    private final String razonSocialEmpresa;

    public PedidoPdfGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReportePedidoDTO reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 36);
        try {
            PdfWriter.getInstance(documento, salida);

            HeaderFooter pie = new HeaderFooter(
                    new Phrase("Reporte de Pedidos - Página ", FUENTE_PIE),
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

            Paragraph pTitulo = new Paragraph("REPORTE DE PEDIDOS Y VENTAS", FUENTE_TITULO);
            pTitulo.setSpacingAfter(10);
            documento.add(pTitulo);

            // Metadatos
            PdfPTable tablaMeta = new PdfPTable(4);
            tablaMeta.setWidthPercentage(100);
            tablaMeta.setSpacingAfter(12);

            agregarMetadato(tablaMeta, "Generado:", reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_FECHA));
            agregarMetadato(tablaMeta, "Estado:", reporte.getEstadoFiltro());
            agregarMetadato(tablaMeta, "Cliente:", reporte.getClienteFiltro());
            agregarMetadato(tablaMeta, "Total Pedidos:", String.valueOf(reporte.getTotalPedidos()));
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
            for (ReportePedidoItemDTO item : reporte.getItems()) {
                Color fondo = alterna ? COLOR_FILA_ALTERNA : Color.WHITE;

                tabla.addCell(celdaDato("#" + item.getIdPedido(), Element.ALIGN_CENTER, fondo));
                tabla.addCell(celdaDato(item.getFechaPedido(), Element.ALIGN_CENTER, fondo));
                tabla.addCell(celdaDato(item.getCliente(), Element.ALIGN_LEFT, fondo));
                tabla.addCell(celdaDato(item.getAlmacen(), Element.ALIGN_LEFT, fondo));
                tabla.addCell(celdaDato(item.getEstado(), Element.ALIGN_CENTER, fondo));
                tabla.addCell(celdaDato("S/ " + formatearMonto(item.getTotal()), Element.ALIGN_RIGHT, fondo));

                alterna = !alterna;
            }

            // Fila de Total General
            PdfPCell celdaTotalLabel = new PdfPCell(new Phrase("TOTAL GENERAL:", FUENTE_ETIQUETA));
            celdaTotalLabel.setColspan(5);
            celdaTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotalLabel.setPadding(6);
            celdaTotalLabel.setBackgroundColor(COLOR_FILA_ALTERNA);
            tabla.addCell(celdaTotalLabel);

            PdfPCell celdaTotalMonto = new PdfPCell(new Phrase("S/ " + formatearMonto(reporte.getMontoTotalGeneral()), FUENTE_ETIQUETA));
            celdaTotalMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotalMonto.setPadding(6);
            celdaTotalMonto.setBackgroundColor(COLOR_FILA_ALTERNA);
            tabla.addCell(celdaTotalMonto);

            documento.add(tabla);
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el reporte PDF de pedidos", e);
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

    private PdfPCell celdaDato(String texto, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", FUENTE_NORMAL));
        celda.setHorizontalAlignment(alineacion);
        celda.setBackgroundColor(fondo);
        celda.setPadding(4);
        return celda;
    }

    private String formatearMonto(BigDecimal monto) {
        return monto != null ? String.format(Locale.US, "%,.2f", monto) : "0.00";
    }
}
