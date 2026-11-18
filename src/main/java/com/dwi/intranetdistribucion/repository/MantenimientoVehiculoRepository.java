package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.MantenimientoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MantenimientoVehiculoRepository extends JpaRepository<MantenimientoVehiculo, Integer> {

    List<MantenimientoVehiculo> findAllByOrderByFechaInicioDesc();

    List<MantenimientoVehiculo> findByVehiculoIdVehiculoOrderByFechaInicioDesc(Integer idVehiculo);

    boolean existsByVehiculoIdVehiculoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Integer idVehiculo, LocalDate desde, LocalDate hasta);
}