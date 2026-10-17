package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Sede;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {

    List<Sede> findAllByOrderByNombreAsc();

    List<Sede> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdSedeNot(String nombre, Integer idSede);

    @EntityGraph(attributePaths = {"responsable"})
    @Query("SELECT s FROM Sede s ORDER BY s.nombre ASC")
    List<Sede> findAllWithResponsable();

    @EntityGraph(attributePaths = {"responsable"})
    @Query("SELECT s FROM Sede s WHERE s.activo = true ORDER BY s.nombre ASC")
    List<Sede> findByActivoTrueWithResponsable();

    @EntityGraph(attributePaths = {"responsable"})
    @Query("SELECT s FROM Sede s WHERE s.idSede = :id")
    Optional<Sede> findByIdWithResponsable(@Param("id") Integer id);

    @Query(value = "SELECT COUNT(*) FROM usuario WHERE id_sede = :idSede", nativeQuery = true)
    long contarUsuariosAsociados(@Param("idSede") Integer idSede);

    @Query(value = "SELECT COUNT(*) FROM almacen WHERE id_sede = :idSede", nativeQuery = true)
    long contarAlmacenesAsociados(@Param("idSede") Integer idSede);

    @Query(value = "SELECT COUNT(*) FROM inventario i JOIN almacen a ON a.id_almacen = i.id_almacen WHERE a.id_sede = :idSede", nativeQuery = true)
    long contarInventarioAsociado(@Param("idSede") Integer idSede);

    @Query(value = "SELECT COALESCE(SUM(i.stock_actual), 0) FROM inventario i JOIN almacen a ON a.id_almacen = i.id_almacen WHERE a.id_sede = :idSede", nativeQuery = true)
    BigDecimal sumarStockInventario(@Param("idSede") Integer idSede);
}
