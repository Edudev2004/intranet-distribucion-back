package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Integer> {

    List<Zona> findAllByOrderByNombreAsc();

    Optional<Zona> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdZonaNot(String nombre, Integer idZona);
}
