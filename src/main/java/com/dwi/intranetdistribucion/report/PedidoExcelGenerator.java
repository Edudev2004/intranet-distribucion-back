package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.ReportePedidoDTO;
import com.dwi.intranetdistribucion.dto.response.ReportePedidoItemDTO;
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
public class PedidoExcelGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final XSSFColor COLOR_CABECERA = new XSSFColor(new byte[]{(byte) 33, (byte) 64, (byte) 115}, null);
    private static final XSSFColor COLOR_BLANCO = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
    private static final String FORMATO_MONEDA = "S/ #,##0.00";

    private static final String[] COLUMNAS = {"N° Pedido", "Fecha", "Cliente", "Almacén", "Estado", "Total"};
    private static final int[] ANCHOS = {15, 20, 35, 25, 18, 18};
    private static final int FILA_CABECERA = 5;

    private final String razonSocialEmpresa;

    public PedidoExcelGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReportePedidoDTO reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            Sheet hoja = libro.createSheet("Pedidos");

            Row titulo = hoja.createRow(0);
            crearCelda(titulo, 0, razonSocialEmpresa + " - Reporte de Pedidos", estilos.titulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMNAS.length - 1));

            crearCelda(hoja.createRow(1), 0, "Generado: "
                    + reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_FECHA), estilos.info);
            crearCelda(hoja.createRow(2), 0, "Estado: " + reporte.getEstadoFiltro() + " | Cliente: " + reporte.getClienteFiltro(), estilos.info);
            crearCelda(hoja.createRow(3), 0, "Total de pedidos: " + reporte.getTotalPedidos(), estilos.info);

            // Cabecera
            Row cabecera = hoja.createRow(FILA_CABECERA);
            for (int i = 0; i < COLUMNAS.length; i++) {
                crearCelda(cabecera, i, COLUMNAS[i], estilos.cabecera);
            }

            int nroFila = FILA_CABECERA + 1;
            for (ReportePedidoItemDTO item : reporte.getItems()) {
                Row fila = hoja.createRow(nroFila++);
                crearCelda(fila, 0, item.getIdPedido(), estilos.datoCentro);
                crearCelda(fila, 1, item.getFechaPedido(), estilos.datoCentro);
                crearCelda(fila, 2, item.getCliente(), estilos.datoTexto);
                crearCelda(fila, 3, item.getAlmacen(), estilos.datoTexto);
                crearCelda(fila, 4, item.getEstado(), estilos.datoCentro);
                crearCelda(fila, 5, item.getTotal() != null ? item.getTotal().doubleValue() : 0.0, estilos.datoMoneda);
            }

            // Fila Total
            Row filaTotal = hoja.createRow(nroFila);
            crearCelda(filaTotal, 0, "TOTAL GENERAL", estilos.total);
            hoja.addMergedRegion(new CellRangeAddress(nroFila, nroFila, 0, 4));
            crearCelda(filaTotal, 5, reporte.getMontoTotalGeneral() != null ? reporte.getMontoTotalGeneral().doubleValue() : 0.0, estilos.totalMoneda);

            for (int i = 0; i < ANCHOS.length; i++) {
                hoja.setColumnWidth(i, ANCHOS[i] * 256);
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de pedidos", e);
        }
    }

    private void crearCelda(Row fila, int col, Object valor, CellStyle estilo) {
        Cell celda = fila.createCell(col);
        if (valor instanceof Number num) {
            celda.setCellValue(num.doubleValue());
        } else if (valor != null) {
            celda.setCellValue(valor.toString());
        }
        celda.setCellStyle(estilo);
    }

    private static class Estilos {
        final CellStyle titulo;
        final CellStyle info;
        final CellStyle cabecera;
        final CellStyle datoTexto;
        final CellStyle datoCentro;
        final CellStyle datoMoneda;
        final CellStyle total;
        final CellStyle totalMoneda;

        Estilos(XSSFWorkbook libro) {
            XSSFFont fTitulo = libro.createFont();
            fTitulo.setFontHeightInPoints((short) 13);
            fTitulo.setBold(true);

            titulo = libro.createCellStyle();
            titulo.setFont(fTitulo);

            XSSFFont fInfo = libro.createFont();
            fInfo.setFontHeightInPoints((short) 9);
            fInfo.setItalic(true);

            info = libro.createCellStyle();
            info.setFont(fInfo);

            XSSFFont fCab = libro.createFont();
            fCab.setBold(true);
            fCab.setColor(COLOR_BLANCO);

            cabecera = libro.createCellStyle();
            ((XSSFCellStyle) cabecera).setFillForegroundColor(COLOR_CABECERA);
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setAlignment(HorizontalAlignment.CENTER);
            cabecera.setFont(fCab);
            aplicarBordes(cabecera);

            datoTexto = libro.createCellStyle();
            aplicarBordes(datoTexto);

            datoCentro = libro.createCellStyle();
            datoCentro.setAlignment(HorizontalAlignment.CENTER);
            aplicarBordes(datoCentro);

            datoMoneda = libro.createCellStyle();
            datoMoneda.setAlignment(HorizontalAlignment.RIGHT);
            datoMoneda.setDataFormat(libro.createDataFormat().getFormat(FORMATO_MONEDA));
            aplicarBordes(datoMoneda);

            XSSFFont fTotal = libro.createFont();
            fTotal.setBold(true);

            total = libro.createCellStyle();
            total.setAlignment(HorizontalAlignment.RIGHT);
            total.setFont(fTotal);
            aplicarBordes(total);

            totalMoneda = libro.createCellStyle();
            totalMoneda.setAlignment(HorizontalAlignment.RIGHT);
            totalMoneda.setFont(fTotal);
            totalMoneda.setDataFormat(libro.createDataFormat().getFormat(FORMATO_MONEDA));
            aplicarBordes(totalMoneda);
        }

        private void aplicarBordes(CellStyle estilo) {
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
        }
    }
}
