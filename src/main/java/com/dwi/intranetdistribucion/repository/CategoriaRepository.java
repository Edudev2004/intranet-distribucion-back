package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdCategoriaNot(String nombre, Integer idCategoria);

    // Consulta nativa: la entidad Producto pertenece a otro módulo (RF-06)
    @Query(value = "SELECT COUNT(*) FROM producto WHERE id_categoria = :idCategoria", nativeQuery = true)
    long contarProductosAsociados(@Param("idCategoria") Integer idCategoria);
}
