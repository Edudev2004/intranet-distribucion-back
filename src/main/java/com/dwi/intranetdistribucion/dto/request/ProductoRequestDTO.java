package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no debe superar 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @NotNull(message = "La unidad de medida es obligatoria")
    private Integer idUnidad;

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0", message = "El stock mínimo no puede ser negativo")
    private BigDecimal stockMinimo;

    @DecimalMin(value = "0", message = "El peso no puede ser negativo")
    private BigDecimal pesoKg;

    @DecimalMin(value = "0", message = "El precio unitario no puede ser negativo")
    private BigDecimal precioUnitario;

    private Boolean activo;
}
