package com.dwi.intranetdistribucion.report;

import com.dwi.intranetdistribucion.dto.response.ReporteEntregaDTO;
import com.dwi.intranetdistribucion.dto.response.ReporteEntregaItemDTO;
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
public class EntregaExcelGenerator {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final XSSFColor COLOR_CABECERA = new XSSFColor(new byte[]{(byte) 33, (byte) 64, (byte) 115}, null);
    private static final XSSFColor COLOR_BLANCO = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);

    private static final String[] COLUMNAS = {"ID Entrega", "Pedido", "Cliente", "Dirección", "H. Estimada", "H. Real", "Estado", "Cumplimiento", "Notas"};
    private static final int[] ANCHOS = {14, 12, 35, 40, 20, 20, 16, 18, 30};
    private static final int FILA_CABECERA = 5;

    private final String razonSocialEmpresa;

    public EntregaExcelGenerator(@Value("${app.empresa.razon-social:Empresa Distribuidora}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(ReporteEntregaDTO reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            Sheet hoja = libro.createSheet("Entregas");

            Row titulo = hoja.createRow(0);
            crearCelda(titulo, 0, razonSocialEmpresa + " - Reporte de Entregas y Cumplimiento", estilos.titulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMNAS.length - 1));

            crearCelda(hoja.createRow(1), 0, "Generado: "
                    + reporte.getGeneradoEn().atZoneSameInstant(ZONA_PERU).format(FORMATO_FECHA), estilos.info);
            crearCelda(hoja.createRow(2), 0, "Total entregas: " + reporte.getTotalEntregas()
                    + " | A tiempo: " + reporte.getTotalATiempo()
                    + " | Con retraso: " + reporte.getTotalConRetraso(), estilos.info);
            crearCelda(hoja.createRow(3), 0, "% Cumplimiento general: " + reporte.getPorcentajeCumplimiento() + " %", estilos.info);

            // Cabecera
            Row cabecera = hoja.createRow(FILA_CABECERA);
            for (int i = 0; i < COLUMNAS.length; i++) {
                crearCelda(cabecera, i, COLUMNAS[i], estilos.cabecera);
            }

            int nroFila = FILA_CABECERA + 1;
            for (ReporteEntregaItemDTO item : reporte.getItems()) {
                Row fila = hoja.createRow(nroFila++);
                crearCelda(fila, 0, item.getIdEntrega(), estilos.datoCentro);
                crearCelda(fila, 1, item.getIdPedido(), estilos.datoCentro);
                crearCelda(fila, 2, item.getCliente(), estilos.datoTexto);
                crearCelda(fila, 3, item.getDireccion(), estilos.datoTexto);
                crearCelda(fila, 4, item.getHoraEstimada(), estilos.datoCentro);
                crearCelda(fila, 5, item.getHoraEntregaReal(), estilos.datoCentro);
                crearCelda(fila, 6, item.getEstadoEntrega(), estilos.datoCentro);
                crearCelda(fila, 7, item.getCumplimiento(), estilos.datoCentro);
                crearCelda(fila, 8, item.getNotas(), estilos.datoTexto);
            }

            for (int i = 0; i < ANCHOS.length; i++) {
                hoja.setColumnWidth(i, ANCHOS[i] * 256);
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de entregas", e);
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
        }

        private void aplicarBordes(CellStyle estilo) {
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
        }
    }
}
