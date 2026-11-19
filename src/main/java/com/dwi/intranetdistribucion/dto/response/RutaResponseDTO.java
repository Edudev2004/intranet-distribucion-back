package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaResponseDTO {

    private Integer idRuta;
    private String nombre;
    private Integer idZona;
    private String nombreZona;
    private Boolean activo;
}
