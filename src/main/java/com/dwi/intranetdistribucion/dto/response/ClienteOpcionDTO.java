package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteOpcionDTO {

    private Integer idCliente;
    private String nombre;
    private String documento;
}