package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Integer> {

    List<Conductor> findAllByOrderByApellidosAscNombresAsc();

    List<Conductor> findByActivoTrueOrderByApellidosAscNombresAsc();

    Optional<Conductor> findByDni(String dni);

    Optional<Conductor> findByNroLicenciaIgnoreCase(String nroLicencia);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdConductorNot(String dni, Integer idConductor);

    boolean existsByNroLicenciaIgnoreCase(String nroLicencia);

    boolean existsByNroLicenciaIgnoreCaseAndIdConductorNot(String nroLicencia, Integer idConductor);
}
