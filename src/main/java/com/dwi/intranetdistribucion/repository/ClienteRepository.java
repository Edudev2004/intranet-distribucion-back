package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Cliente;
import com.dwi.intranetdistribucion.repository.projection.ClienteOpcionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndIdClienteNot(String numeroDocumento, Integer idCliente);

    List<Cliente> findByActivoTrueOrderByNombreRazonSocialAsc();

    List<Cliente> findAllByOrderByNombreRazonSocialAsc();

    @Query("""
        SELECT c FROM Cliente c
        WHERE (:soloActivos = false OR c.activo = true)
          AND (:filtro IS NULL OR LOWER(c.nombreRazonSocial) LIKE LOWER(CONCAT('%', :filtro, '%'))
               OR c.numeroDocumento LIKE CONCAT('%', :filtro, '%'))
        ORDER BY c.nombreRazonSocial ASC
    """)
    List<Cliente> buscarConFiltro(@Param("filtro") String filtro, @Param("soloActivos") boolean soloActivos);

    @Query(value = """
        SELECT c.id_cliente AS "idCliente",
               c.nombre_razon_social AS "nombre",
               c.numero_documento AS "documento"
        FROM cliente c
        WHERE c.activo = TRUE
        ORDER BY c.nombre_razon_social
        """, nativeQuery = true)
    List<ClienteOpcionView> listarOpciones();
}
