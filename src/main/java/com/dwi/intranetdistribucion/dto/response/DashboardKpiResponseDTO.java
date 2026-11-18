package com.dwi.intranetdistribucion.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardKpiResponseDTO {
    private long totalPedidos;
    private long pedidosAprobados;
    private long pedidosPendientes;
    private long totalEntregasExitosas;
    private long totalEntregasConIncidencia;
    private long vehiculosEnRuta;
    private long conductoresActivos;
}