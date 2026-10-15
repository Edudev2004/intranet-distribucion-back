package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Set<PermisoResponseDTO> permisos;
}
