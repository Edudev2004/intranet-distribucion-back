package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsableSedeResponseDTO {

    private Integer idUsuario;
    private String nombreCompleto;
    private String username;
    private String correo;
    private String rol;
}
