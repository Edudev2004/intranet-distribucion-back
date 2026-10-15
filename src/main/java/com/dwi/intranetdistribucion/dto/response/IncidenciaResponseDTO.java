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
public class IncidenciaResponseDTO {

    private Long idIncidencia;
    private Long idEntrega;
    private Long idPedido;
    private Integer idProgramacion;
    private Short idTipoIncidencia;
    private String tipoIncidencia;
    private String comentario;
    private OffsetDateTime fechaHora;
    private Integer idUsuario;
    private String username;
    private String nombreUsuario;
}
