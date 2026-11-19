package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer> {

    List<Ruta> findAllByOrderByNombreAsc();

    List<Ruta> findByActivoTrueOrderByNombreAsc();

    List<Ruta> findByZona_IdZonaOrderByNombreAsc(Integer idZona);

    List<Ruta> findByZona_IdZonaAndActivoTrueOrderByNombreAsc(Integer idZona);

    boolean existsByNombreIgnoreCaseAndZona_IdZona(String nombre, Integer idZona);

    boolean existsByNombreIgnoreCaseAndZona_IdZonaAndIdRutaNot(String nombre, Integer idZona, Integer idRuta);
}
