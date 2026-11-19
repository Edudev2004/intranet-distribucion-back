package com.dwi.intranetdistribucion.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarPedidosRequestDTO {

    @NotEmpty(message = "Debe proporcionar al menos un ID de pedido para asignar a la ruta")
    private List<Long> idPedidos;
}
