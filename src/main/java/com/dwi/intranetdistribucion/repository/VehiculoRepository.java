package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    List<Vehiculo> findAllByOrderByPlacaAsc();

    List<Vehiculo> findByEstado_IdEstadoVehiculoOrderByPlacaAsc(Short idEstadoVehiculo);

    Optional<Vehiculo> findByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdVehiculoNot(String placa, Integer idVehiculo);
}
