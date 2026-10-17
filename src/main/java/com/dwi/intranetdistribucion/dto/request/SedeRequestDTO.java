package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SedeRequestDTO {

    @NotBlank(message = "El nombre de la sede es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La dirección de la sede es obligatoria")
    @Size(min = 3, max = 255, message = "La dirección debe tener entre 3 y 255 caracteres")
    private String direccion;

    private Integer idResponsable;

    private Boolean activo;
}
