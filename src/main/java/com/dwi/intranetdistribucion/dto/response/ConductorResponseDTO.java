package com.dwi.intranetdistribucion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductorResponseDTO {

    private Integer idConductor;
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String dni;
    private String nroLicencia;
    private String categoriaLicencia;
    private LocalDate fechaVencLicencia;
    private boolean licenciaVencida;
    private String telefono;
    private Boolean activo;
}
