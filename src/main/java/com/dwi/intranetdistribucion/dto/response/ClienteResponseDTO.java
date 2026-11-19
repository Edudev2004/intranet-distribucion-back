package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {

    private Integer idCliente;
    private Short idTipoDocumento;
    private String tipoDocumentoNombre;
    private String numeroDocumento;
    private String nombreRazonSocial;
    private String direccion;
    private String telefono;
    private String correo;
    private Integer idZona;
    private String zonaNombre;
    private Boolean activo;
}
