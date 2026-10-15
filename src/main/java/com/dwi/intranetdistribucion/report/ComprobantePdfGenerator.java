package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.model.ClaseComprobante;
import com.dwi.intranetdistribucion.model.Comprobante;
import com.dwi.intranetdistribucion.repository.projection.PedidoCabeceraView;
import com.dwi.intranetdistribucion.repository.projection.PedidoDetalleView;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
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
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Genera la representación impresa (PDF) de un comprobante o guía de remisión
@Component
public class ComprobantePdfGenerator {

    // Se asume que detalle_pedido.precio_unitario no incluye IGV
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COLOR_CABECERA = new Color(33, 64, 115);
    private static final Color COLOR_FILA_ALTERNA = new Color(242, 245, 250);

    private static final Font FUENTE_EMPRESA = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_CABECERA);
    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font FUENTE_ETIQUETA = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font FUENTE_TABLA_CABECERA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_PIE = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);

    private final String razonSocialEmpresa;
    private final String rucEmpresa;

    public ComprobantePdfGenerator(
            @Value("${app.empresa.razon-social:Empresa Distribuidora}") String razonSocialEmpresa,
            @Value("${app.empresa.ruc:00000000000}") String rucEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
        this.rucEmpresa = rucEmpresa;
    }

    public byte[] generar(Comprobante comprobante, ClaseComprobante clase,
                          PedidoCabeceraView cabecera, List<PedidoDetalleView> detalle) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(construirEncabezado(comprobante));
            documento.add(espacio());
            documento.add(construirDatosGenerales(comprobante, cabecera));
            if (clase.esGuia()) {
                documento.add(espacio());
                documento.add(construirDatosTraslado(cabecera));
                documento.add(espacio());
                documento.add(construirDetalleGuia(detalle));
            } else {
                documento.add(espacio());
                documento.add(construirDetalleVenta(detalle));
                documento.add(construirTotales(detalle));
            }
            documento.add(espacio());
            documento.add(construirPie(comprobante));

            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del comprobante "
                    + comprobante.getNumeroCompleto(), e);
        }
        return salida.toByteArray();
    }

    private PdfPTable construirEncabezado(Comprobante comprobante) {
        PdfPTable tabla = new PdfPTable(new float[]{3f, 2f});
        tabla.setWidthPercentage(100);

        PdfPCell empresa = new PdfPCell();
        empresa.setBorder(Rectangle.NO_BORDER);
        empresa.addElement(new Paragraph(razonSocialEmpresa, FUENTE_EMPRESA));
        empresa.addElement(new Paragraph("Intranet de Distribución", FUENTE_NORMAL));
        tabla.addCell(empresa);

        PdfPCell recuadro = new PdfPCell();
        recuadro.setBorderColor(COLOR_CABECERA);
        recuadro.setBorderWidth(1.5f);
        recuadro.setPadding(8);
        recuadro.addElement(parrafoCentrado("RUC " + rucEmpresa, FUENTE_TITULO));
        recuadro.addElement(parrafoCentrado(comprobante.getTipoComprobante().getNombre().toUpperCase(Locale.ROOT), FUENTE_TITULO));
        recuadro.addElement(parrafoCentrado(comprobante.getNumeroCompleto(), FUENTE_TITULO));
        tabla.addCell(recuadro);
        return tabla;
    }

    private PdfPTable construirDatosGenerales(Comprobante comprobante, PedidoCabeceraView cabecera) {
        PdfPTable tabla = tablaEtiquetaValor();
        agregarFila(tabla, "Fecha de emisión", comprobante.getFechaEmision()
                .atZoneSameInstant(ZONA_PERU).format(FORMATO_FECHA));
        agregarFila(tabla, "Cliente", cabecera.getCliente());
        agregarFila(tabla, cabecera.getTipoDocumentoCliente(), cabecera.getNumeroDocumentoCliente());
        agregarFila(tabla, "Dirección", cabecera.getDireccionCliente());
        agregarFila(tabla, "Teléfono", cabecera.getTelefonoCliente());
        agregarFila(tabla, "Pedido N°", String.valueOf(cabecera.getIdPedido()));
        agregarFila(tabla, "Fecha del pedido", cabecera.getFechaPedido());
        agregarFila(tabla, "Entrega solicitada", cabecera.getFechaEntregaSolicitada());
        agregarFila(tabla, "Almacén", cabecera.getAlmacen() + " - " + cabecera.getSede());
        agregarFila(tabla, "Observación", cabecera.getObservacion());
        return tabla;
    }

    private PdfPTable construirDatosTraslado(PedidoCabeceraView cabecera) {
        PdfPTable tabla = tablaEtiquetaValor();
        agregarFila(tabla, "Motivo de traslado", "Venta");
        agregarFila(tabla, "Punto de partida", cabecera.getDireccionSede() + " (" + cabecera.getAlmacen() + ")");
        agregarFila(tabla, "Punto de llegada", cabecera.getDireccionCliente());
        if (cabecera.getPlacaVehiculo() != null) {
            agregarFila(tabla, "Vehículo (placa)", cabecera.getPlacaVehiculo());
            agregarFila(tabla, "Conductor", cabecera.getConductor());
            agregarFila(tabla, "Licencia", cabecera.getLicenciaConductor());
        } else {
            agregarFila(tabla, "Transporte", "Pendiente de programación de ruta");
        }
        return tabla;
    }

    private PdfPTable construirDetalleVenta(List<PedidoDetalleView> detalle) {
        PdfPTable tabla = new PdfPTable(new float[]{1.3f, 4f, 1f, 1.2f, 1.4f, 1.5f});
        tabla.setWidthPercentage(100);
        agregarCabeceras(tabla, "Código", "Descripción", "Unidad", "Cantidad", "P. Unit.", "Subtotal");
        int fila = 0;
        for (PedidoDetalleView linea : detalle) {
            Color fondo = fila++ % 2 == 0 ? null : COLOR_FILA_ALTERNA;
            tabla.addCell(celda(linea.getCodigo(), Element.ALIGN_LEFT, fondo));
            tabla.addCell(celda(linea.getProducto(), Element.ALIGN_LEFT, fondo));
            tabla.addCell(celda(linea.getUnidad(), Element.ALIGN_CENTER, fondo));
            tabla.addCell(celda(cantidad(linea.getCantidad()), Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celda(moneda(linea.getPrecioUnitario()), Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celda(moneda(linea.getSubtotal()), Element.ALIGN_RIGHT, fondo));
        }
        return tabla;
    }

    private PdfPTable construirTotales(List<PedidoDetalleView> detalle) {
        BigDecimal gravada = detalle.stream()
                .map(PedidoDetalleView::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = gravada.multiply(TASA_IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = gravada.add(igv);

        PdfPTable tabla = new PdfPTable(new float[]{8.9f, 2.4f, 1.5f});
        tabla.setWidthPercentage(100);
        agregarTotal(tabla, "Op. gravada", moneda(gravada), FUENTE_NORMAL);
        agregarTotal(tabla, "IGV (18%)", moneda(igv), FUENTE_NORMAL);
        agregarTotal(tabla, "Importe total", moneda(total), FUENTE_ETIQUETA);
        return tabla;
    }

    private PdfPTable construirDetalleGuia(List<PedidoDetalleView> detalle) {
        PdfPTable tabla = new PdfPTable(new float[]{1.3f, 5f, 1f, 1.3f, 1.4f});
        tabla.setWidthPercentage(100);
        agregarCabeceras(tabla, "Código", "Descripción", "Unidad", "Cantidad", "Peso (kg)");
        BigDecimal pesoTotal = BigDecimal.ZERO;
        int fila = 0;
        for (PedidoDetalleView linea : detalle) {
            Color fondo = fila++ % 2 == 0 ? null : COLOR_FILA_ALTERNA;
            tabla.addCell(celda(linea.getCodigo(), Element.ALIGN_LEFT, fondo));
            tabla.addCell(celda(linea.getProducto(), Element.ALIGN_LEFT, fondo));
            tabla.addCell(celda(linea.getUnidad(), Element.ALIGN_CENTER, fondo));
            tabla.addCell(celda(cantidad(linea.getCantidad()), Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celda(decimal(linea.getPesoKg()), Element.ALIGN_RIGHT, fondo));
            pesoTotal = pesoTotal.add(linea.getPesoKg());
        }
        PdfPCell etiqueta = new PdfPCell(new Phrase("Peso bruto total (kg)", FUENTE_ETIQUETA));
        etiqueta.setColspan(4);
        etiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        etiqueta.setPadding(4);
        tabla.addCell(etiqueta);
        PdfPCell valor = new PdfPCell(new Phrase(decimal(pesoTotal), FUENTE_ETIQUETA));
        valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valor.setPadding(4);
        tabla.addCell(valor);
        return tabla;
    }

    private Paragraph construirPie(Comprobante comprobante) {
        Paragraph pie = new Paragraph("Emitido por: " + comprobante.getUsuario().getNombres() + " "
                + comprobante.getUsuario().getApellidos() + " (" + comprobante.getUsuario().getUsername() + ")"
                + "\nRepresentación impresa generada por la Intranet de Distribución. "
                + "No reemplaza al comprobante electrónico emitido ante SUNAT.", FUENTE_PIE);
        pie.setAlignment(Element.ALIGN_CENTER);
        return pie;
    }

    private PdfPTable tablaEtiquetaValor() {
        PdfPTable tabla = new PdfPTable(new float[]{1.3f, 4f});
        tabla.setWidthPercentage(100);
        return tabla;
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, FUENTE_ETIQUETA));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setPadding(2);
        tabla.addCell(celdaEtiqueta);
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor == null || valor.isBlank() ? "-" : valor, FUENTE_NORMAL));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPadding(2);
        tabla.addCell(celdaValor);
    }

    private void agregarCabeceras(PdfPTable tabla, String... titulos) {
        for (String titulo : titulos) {
            PdfPCell cabecera = new PdfPCell(new Phrase(titulo, FUENTE_TABLA_CABECERA));
            cabecera.setBackgroundColor(COLOR_CABECERA);
            cabecera.setHorizontalAlignment(Element.ALIGN_CENTER);
            cabecera.setPadding(5);
            tabla.addCell(cabecera);
        }
        tabla.setHeaderRows(1);
    }

    private void agregarTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        PdfPCell vacia = new PdfPCell();
        vacia.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(vacia);
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuente));
        celdaEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaEtiqueta.setPadding(4);
        tabla.addCell(celdaEtiqueta);
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaValor.setPadding(4);
        tabla.addCell(celdaValor);
    }

    private PdfPCell celda(String texto, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, FUENTE_NORMAL));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(4);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private Paragraph parrafoCentrado(String texto, Font fuente) {
        Paragraph parrafo = new Paragraph(texto, fuente);
        parrafo.setAlignment(Element.ALIGN_CENTER);
        return parrafo;
    }

    private Paragraph espacio() {
        return new Paragraph(" ", FUENTE_NORMAL);
    }

    private String moneda(BigDecimal valor) {
        return "S/ " + String.format(Locale.US, "%,.2f", valor);
    }

    private String decimal(BigDecimal valor) {
        return String.format(Locale.US, "%,.2f", valor);
    }

    private String cantidad(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
