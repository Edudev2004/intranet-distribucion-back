package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Short> {

    @Query("SELECT p FROM Permiso p WHERE CONCAT(p.modulo.nombre, '_', p.accion.nombre) = :nombre")
    Optional<Permiso> findByNombre(@Param("nombre") String nombre);

    @Query("SELECT COUNT(p) > 0 FROM Permiso p WHERE CONCAT(p.modulo.nombre, '_', p.accion.nombre) = :nombre")
    boolean existsByNombre(@Param("nombre") String nombre);

    @Query("SELECT p FROM Permiso p WHERE p.modulo.nombre = :modulo")
    List<Permiso> findByModulo(@Param("modulo") String modulo);

    @Query("SELECT p FROM Permiso p WHERE p.modulo.nombre = :modulo AND p.accion.nombre = :accion")
    Optional<Permiso> findByModulo_NombreAndAccion_Nombre(@Param("modulo") String modulo, @Param("accion") String accion);

    default Optional<Permiso> findById(Long id) {
        return id != null ? findById(id.shortValue()) : Optional.empty();
    }
}
