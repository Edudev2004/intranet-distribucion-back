package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequestDTO {

    @NotNull(message = "El almacén es obligatorio")
    private Integer idAlmacen;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private Short idTipoMovimiento;

    private Long idPedido;

    private String observacion;

    @NotEmpty(message = "Debe incluir al menos un producto en el movimiento")
    @Valid
    private List<MovimientoItemRequestDTO> items;
}
