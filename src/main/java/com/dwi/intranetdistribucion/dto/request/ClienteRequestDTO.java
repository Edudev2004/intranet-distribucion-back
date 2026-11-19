package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class ClienteRequestDTO {

    @NotNull(message = "El tipo de documento es obligatorio")
    private Short idTipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "El nombre o razón social es obligatorio")
    @Size(max = 255, message = "El nombre o razón social no puede exceder 255 caracteres")
    private String nombreRazonSocial;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;

    private String telefono;

    private String correo;

    private Integer idZona;

    private Boolean activo;
}
