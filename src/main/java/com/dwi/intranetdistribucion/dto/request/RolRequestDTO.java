package com.dwi.intranetdistribucion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolRequestDTO {
    private String nombre;
    private String descripcion;
    private Set<Long> permisoIds;
}
