package com.dwi.intranetdistribucion.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoResponseDTO {

    private Long idMovimiento;
    private Integer idAlmacen;
    private String almacen;
    private Short idTipoMovimiento;
    private String tipoMovimiento;
    private Character naturaleza; // 'E' or 'S'
    private Integer idUsuario;
    private String usuario;
    private Long idPedido;
    private OffsetDateTime fecha;
    private String observacion;
    private List<MovimientoDetalleResponseDTO> items;
    private boolean contieneAlertas;
}
