package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.TipoAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoAlmacenRepository extends JpaRepository<TipoAlmacen, Short> {
}