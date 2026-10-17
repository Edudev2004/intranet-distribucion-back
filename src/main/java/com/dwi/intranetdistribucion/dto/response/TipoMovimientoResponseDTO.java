package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMovimientoResponseDTO {

    private Short idTipoMovimiento;
    private String nombre;
    private Character naturaleza; // 'E' = Entrada, 'S' = Salida
}
