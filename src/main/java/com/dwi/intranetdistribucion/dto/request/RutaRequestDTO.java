package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RutaRequestDTO {

    @NotBlank(message = "El nombre de la ruta es obligatorio")
    @Size(max = 100, message = "El nombre de la ruta no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "El ID de la zona es obligatorio")
    private Integer idZona;

    private Boolean activo = true;
}
