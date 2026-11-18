package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AprobacionPedidoRequestDTO {

    @NotBlank(message = "La decisión es obligatoria")
    @Pattern(regexp = "APROBADO|RECHAZADO", message = "La decisión debe ser APROBADO o RECHAZADO")
    private String decision;

    private String motivo;
}