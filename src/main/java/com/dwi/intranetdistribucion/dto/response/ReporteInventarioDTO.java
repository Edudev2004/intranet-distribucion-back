package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

// Reporte completo: filtros aplicados, resumen y filas
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteInventarioDTO {

    private OffsetDateTime generadoEn;
    private String almacenFiltro;
    private String categoriaFiltro;
    private Integer totalRegistros;
    private Integer totalBajoStock;
    private List<InventarioReporteDTO> items;
}
