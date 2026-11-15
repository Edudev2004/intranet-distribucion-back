package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMantenimientoResponseDTO {

    private Short idTipoMantenimiento;
    private String nombre;
}