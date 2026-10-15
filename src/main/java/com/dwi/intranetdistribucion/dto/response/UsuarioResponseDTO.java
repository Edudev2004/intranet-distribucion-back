package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String username;
    private String correo;
    private Short idRol;
    private String rol;
    private Integer idSede;
    private String sede;
    private Boolean activo;
    private Short intentosFallidos;
    private Boolean bloqueado;
    private OffsetDateTime ultimoAcceso;
    private OffsetDateTime creadoEn;
}
