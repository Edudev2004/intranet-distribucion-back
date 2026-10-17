package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_movimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_movimiento")
    private Short idTipoMovimiento;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false, unique = true)
    private String nombre;

    @Column(name = "naturaleza", columnDefinition = "char(1)", nullable = false)
    private Character naturaleza; // 'E' = Entrada, 'S' = Salida
}
