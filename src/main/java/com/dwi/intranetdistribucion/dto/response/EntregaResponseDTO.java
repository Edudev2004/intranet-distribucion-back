package com.dwi.intranetdistribucion.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class EntregaResponseDTO {
    private Long idEntrega;
    private Integer idProgramacion;
    private Long idPedido;
    private Short idEstadoEntrega;
    private String estadoEntrega;
    private Short ordenVisita;
    private OffsetDateTime horaEstimada;
    private OffsetDateTime horaEntregaReal;
    private String fotoEntregaUrl;
    private String fotoGuiaUrl;
    private String notas;
    private String cliente;
    private String direccionCliente;
    private String telefonoCliente;
}