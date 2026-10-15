package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Short> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    default Optional<Rol> findById(Long id) {
        return id != null ? findById(id.shortValue()) : Optional.empty();
    }

    default boolean existsById(Long id) {
        return id != null && existsById(id.shortValue());
    }

    default void deleteById(Long id) {
        if (id != null) {
            deleteById(id.shortValue());
        }
    }
}
