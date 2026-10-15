package com.dwi.intranetdistribucion.repository;

import com.dwi.intranetdistribucion.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Short> {
}
