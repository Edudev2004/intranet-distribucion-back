package com.dwi.intranetdistribucion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permiso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Short idPermiso;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_modulo", nullable = false)
    private Modulo modulo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_accion", nullable = false)
    private Accion accion;

    // Helper compatibility methods
    public Long getId() {
        return idPermiso != null ? idPermiso.longValue() : null;
    }

    public void setId(Long id) {
        this.idPermiso = id != null ? id.shortValue() : null;
    }

    public String getNombre() {
        if (modulo != null && accion != null) {
            return modulo.getNombre() + "_" + accion.getNombre();
        }
        return null;
    }

    public String getDescripcion() {
        if (modulo != null && accion != null) {
            return accion.getNombre() + " en " + modulo.getNombre();
        }
        return null;
    }

    public String getModuloNombre() {
        return modulo != null ? modulo.getNombre() : null;
    }
}
