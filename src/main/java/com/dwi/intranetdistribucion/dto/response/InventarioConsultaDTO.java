package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioConsultaDTO {

    private List<InventarioItemDTO> items;
    private int totalProductos;
    private int totalBajoStock;
    private int totalSinStock;
    private String almacenFiltro;
    private String categoriaFiltro;
}
