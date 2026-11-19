package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEntregaDTO {

    private OffsetDateTime generadoEn;
    private LocalDate fechaInicioFiltro;
    private LocalDate fechaFinFiltro;
    private String estadoFiltro;
    private int totalEntregas;
    private int totalATiempo;
    private int totalConRetraso;
    private int totalPendientes;
    private BigDecimal porcentajeCumplimiento;
    private List<ReporteEntregaItemDTO> items;
}
