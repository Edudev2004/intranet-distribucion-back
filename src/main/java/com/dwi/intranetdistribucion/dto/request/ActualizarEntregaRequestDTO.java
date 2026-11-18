package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEntregaRequestDTO {

    @NotNull(message = "El estado de entrega es obligatorio")
    private Short idEstadoEntrega;
}