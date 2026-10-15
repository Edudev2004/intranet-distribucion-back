package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlmacenRequestDTO {

    @NotNull(message = "La sede es obligatoria")
    private Integer idSede;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de almacén es obligatorio")
    private Short idTipoAlmacen;

    @NotNull(message = "La capacidad máxima es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La capacidad máxima debe ser mayor que cero")
    private BigDecimal capacidadMaxima;

    @Size(max = 255, message = "La ubicación no debe superar 255 caracteres")
    private String ubicacion;

    private Boolean activo;
}