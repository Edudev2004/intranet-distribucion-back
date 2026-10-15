package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteResponseDTO {

    private Long idComprobante;
    private Long idPedido;
    private Short idTipoComprobante;
    private String tipoComprobante;
    private String serie;
    private Integer numero;
    private String numeroCompleto;
    private OffsetDateTime fechaEmision;
    private Integer idUsuario;
    private String username;
    private String nombreUsuario;
}
