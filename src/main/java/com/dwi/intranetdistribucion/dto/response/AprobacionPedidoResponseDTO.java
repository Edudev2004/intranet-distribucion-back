package com.dwi.intranetdistribucion.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class AprobacionPedidoResponseDTO {
    private Long idAprobacion;
    private Long idPedido;
    private String decision;
    private String motivo;
    private String usuarioAprobador;
    private OffsetDateTime fecha;
}