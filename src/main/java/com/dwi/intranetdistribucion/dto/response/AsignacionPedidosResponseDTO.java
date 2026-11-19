package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionPedidosResponseDTO {

    private Integer idProgramacion;
    private Integer idVehiculo;
    private String vehiculoPlaca;
    private BigDecimal capacidadVehiculoKg;
    private BigDecimal pesoTotalAsignadoKg;
    private BigDecimal pesoDisponibleKg;
    private BigDecimal porcentajeOcupacion;
    private Integer cantidadPedidosAsignados;
    private String mensaje;
    private List<EntregaResponseDTO> entregas;
}
