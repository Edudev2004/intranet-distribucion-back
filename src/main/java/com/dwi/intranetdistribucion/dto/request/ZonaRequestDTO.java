package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonaRequestDTO {

    @NotBlank(message = "El nombre de la zona es obligatorio")
    @Size(max = 100, message = "El nombre de la zona no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;
}
