package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEntregaItemDTO {

    private Long idEntrega;
    private Long idPedido;
    private String cliente;
    private String direccion;
    private String estadoEntrega;
    private String horaEstimada;
    private String horaEntregaReal;
    private String cumplimiento; // "A TIEMPO", "CON RETRASO", "PENDIENTE", "NO ENTREGADO"
    private String notas;
}
