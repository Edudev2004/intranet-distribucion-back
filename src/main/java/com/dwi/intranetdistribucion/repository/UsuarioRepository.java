package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Override
    @EntityGraph(attributePaths = {"rol", "sede"})
    List<Usuario> findAll();

    @Override
    @EntityGraph(attributePaths = {"rol", "sede"})
    Optional<Usuario> findById(Integer id);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, Integer idUsuario);

    boolean existsByUsernameIgnoreCaseAndIdUsuarioNot(String username, Integer idUsuario);

    // Compatibility methods for RF-01 / RF-02
    default Optional<Usuario> findByEmail(String email) {
        return findByCorreoIgnoreCase(email);
    }

    default boolean existsByEmail(String email) {
        return existsByCorreoIgnoreCase(email);
    }

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.rol.idRol = :rolId")
    boolean existsByRol_IdRol(@Param("rolId") Short rolId);

    default boolean existsByRolId(Long rolId) {
        return rolId != null && existsByRol_IdRol(rolId.shortValue());
    }

    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = CAST(COALESCE(u.intentosFallidos, 0) + 1 AS short) WHERE u.idUsuario = :id")
    void incrementarIntentosFallidos(@Param("id") Integer id);

    default void incrementarIntentosFallidos(Long id) {
        if (id != null) {
            incrementarIntentosFallidos(id.intValue());
        }
    }

    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.bloqueado = true, u.fechaBloqueo = CURRENT_TIMESTAMP WHERE u.idUsuario = :id")
    void bloquearUsuario(@Param("id") Integer id);

    default void bloquearUsuario(Long id) {
        if (id != null) {
            bloquearUsuario(id.intValue());
        }
    }

    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.bloqueado = false, u.intentosFallidos = 0, u.fechaBloqueo = null WHERE u.idUsuario = :id")
    void desbloquearUsuario(@Param("id") Integer id);

    default void desbloquearUsuario(Long id) {
        if (id != null) {
            desbloquearUsuario(id.intValue());
        }
    }

    @Transactional
    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = 0 WHERE u.idUsuario = :id")
    void reiniciarIntentosFallidos(@Param("id") Integer id);

    default void reiniciarIntentosFallidos(Long id) {
        if (id != null) {
            reiniciarIntentosFallidos(id.intValue());
        }
    }
}
