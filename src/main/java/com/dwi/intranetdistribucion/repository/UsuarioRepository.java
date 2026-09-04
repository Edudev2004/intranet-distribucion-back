package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRolId(Long rolId);

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = COALESCE(u.intentosFallidos, 0) + 1 WHERE u.id = :id")
    void incrementarIntentosFallidos(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Usuario u SET u.esBloqueado = true WHERE u.id = :id")
    void bloquearUsuario(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = 0 WHERE u.id = :id")
    void reiniciarIntentosFallidos(@Param("id") Long id);
}
