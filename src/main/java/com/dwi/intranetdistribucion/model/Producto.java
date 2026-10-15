package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "codigo", columnDefinition = "varchar", nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "stock_minimo", columnDefinition = "numeric", nullable = false)
    private BigDecimal stockMinimo;

    @Column(name = "peso_kg", columnDefinition = "numeric")
    private BigDecimal pesoKg;
}
