package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_almacen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_almacen")
    private Short idTipoAlmacen;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false, unique = true)
    private String nombre;
}