package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArchivoReporteDTO {

    private String nombreArchivo;
    private byte[] contenido;
}
