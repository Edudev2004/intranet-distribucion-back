package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {
}
