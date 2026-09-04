package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // ej. USUARIOS_VER, PRODUCTOS_CREAR

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String modulo; // ej. SEGURIDAD, INVENTARIO, PEDIDOS, LOGISTICA
}
