package com.dwi.intranetdistribucion.model;

import java.text.Normalizer;
import java.util.Locale;

// Clasifica un tipo_comprobante por su nombre para aplicar reglas y formato de PDF.
public enum ClaseComprobante {

    FACTURA("F001"),
    BOLETA("B001"),
    GUIA_REMISION("T001"),
    OTRO(null);

    private final String seriePorDefecto;

    ClaseComprobante(String seriePorDefecto) {
        this.seriePorDefecto = seriePorDefecto;
    }

    public static ClaseComprobante desde(TipoComprobante tipo) {
        String nombre = Normalizer.normalize(tipo.getNombre(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        if (nombre.contains("FACTURA")) {
            return FACTURA;
        }
        if (nombre.contains("BOLETA")) {
            return BOLETA;
        }
        if (nombre.contains("GUIA")) {
            return GUIA_REMISION;
        }
        return OTRO;
    }

    public String seriePorDefecto(TipoComprobante tipo) {
        if (seriePorDefecto != null) {
            return seriePorDefecto;
        }
        return tipo.getNombre().trim().substring(0, 1).toUpperCase(Locale.ROOT) + "001";
    }

    public boolean esGuia() {
        return this == GUIA_REMISION;
    }
}
