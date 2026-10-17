package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SedeResponseDTO {

    private Integer idSede;
    private String nombre;
    private String direccion;
    private Integer idResponsable;
    private String nombreResponsable;
    private String correoResponsable;
    private Boolean activo;
    private Long totalUsuarios;
    private Long totalAlmacenes;
}
