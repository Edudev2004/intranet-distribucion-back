package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteRequestDTO {

    @NotNull(message = "El pedido es obligatorio")
    private Long idPedido;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private Short idTipoComprobante;

    // Opcional: si no se envía se usa la serie por defecto del tipo (F001, B001, T001)
    @Pattern(regexp = "^[A-Z0-9]{4}$", message = "La serie debe tener 4 caracteres alfanuméricos en mayúscula (ej. F001)")
    private String serie;

    // Usuario que emite; se tomará del token cuando se integre RF-01 (JWT)
    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;
}
