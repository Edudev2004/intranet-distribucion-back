package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombres", columnDefinition = "varchar", nullable = false)
    private String nombres;

    @Column(name = "apellidos", columnDefinition = "varchar", nullable = false)
    private String apellidos;

    @Column(name = "username", columnDefinition = "varchar", nullable = false, unique = true)
    private String username;

    @Column(name = "correo", columnDefinition = "varchar", nullable = false, unique = true)
    private String correo;

    @Column(name = "password_hash", columnDefinition = "varchar", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private Short intentosFallidos;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = OffsetDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.bloqueado == null) {
            this.bloqueado = false;
        }
        if (this.intentosFallidos == null) {
            this.intentosFallidos = 0;
        }
    }
}
