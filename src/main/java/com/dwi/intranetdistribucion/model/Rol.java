package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Short idRol;

    @Column(name = "nombre", columnDefinition = "varchar", nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "varchar")
    private String descripcion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "roles_permisos",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();

    // Compatibility helpers for RF-02
    public Long getId() {
        return idRol != null ? idRol.longValue() : null;
    }

    public void setId(Long id) {
        this.idRol = id != null ? id.shortValue() : null;
    }
}
