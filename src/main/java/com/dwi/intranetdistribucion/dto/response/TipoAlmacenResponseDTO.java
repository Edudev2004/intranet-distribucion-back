package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAlmacenResponseDTO {

    private Short idTipoAlmacen;
    private String nombre;
}