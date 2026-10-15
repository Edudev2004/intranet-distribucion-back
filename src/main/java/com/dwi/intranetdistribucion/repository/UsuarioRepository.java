package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
