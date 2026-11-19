package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ConductorRequestDTO {

    private Integer idUsuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    private String dni;

    @NotBlank(message = "El número de licencia es obligatorio")
    @Size(max = 20, message = "El número de licencia no puede exceder 20 caracteres")
    private String nroLicencia;

    @NotBlank(message = "La categoría de licencia es obligatoria")
    @Size(max = 10, message = "La categoría no puede exceder 10 caracteres")
    private String categoriaLicencia;

    @NotNull(message = "La fecha de vencimiento de la licencia es obligatoria")
    private LocalDate fechaVencLicencia;

    @Pattern(regexp = "^$|^[0-9+ -]{7,15}$", message = "El teléfono debe tener un formato válido")
    private String telefono;

    private Boolean activo = true;
}
