package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteStockRequestDTO {

    @NotNull(message = "El almacén es obligatorio")
    private Integer idAlmacen;

    @NotNull(message = "El producto es obligatorio")
    private Integer idProducto;

    @NotNull(message = "El nuevo stock es obligatorio")
    @DecimalMin(value = "0.0", message = "El stock no puede ser negativo")
    private BigDecimal nuevoStock;

    private String motivo;
}
