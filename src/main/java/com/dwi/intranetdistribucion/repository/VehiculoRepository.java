package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    List<Vehiculo> findAllByOrderByPlacaAsc();
}