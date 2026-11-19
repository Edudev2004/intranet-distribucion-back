package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Short> {

    List<TipoDocumento> findAllByOrderByNombreAsc();

    Optional<TipoDocumento> findByNombreIgnoreCase(String nombre);
}
