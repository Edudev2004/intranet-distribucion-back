package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaRequestDTO {

    @NotNull(message = "La entrega es obligatoria")
    private Long idEntrega;

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private Short idTipoIncidencia;

    @Size(max = 500, message = "El comentario no debe superar 500 caracteres")
    private String comentario;

    // Usuario que registra la incidencia; se tomará del token cuando se integre RF-01 (JWT)
    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;
}
