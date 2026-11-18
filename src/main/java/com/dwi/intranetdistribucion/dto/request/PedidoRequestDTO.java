package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequestDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Integer idCliente;

    @NotNull(message = "El almacén es obligatorio")
    private Integer idAlmacen;

    @FutureOrPresent(message = "La fecha de entrega no puede ser anterior a hoy")
    private LocalDate fechaEntregaSolicitada;

    @Size(max = 255, message = "La observación no debe superar 255 caracteres")
    private String observacion;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<DetallePedidoRequestDTO> detalles;
}