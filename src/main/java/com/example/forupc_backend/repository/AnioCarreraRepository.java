package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnioCarreraRepository extends JpaRepository<AnioCarrera, Integer> {

    List<AnioCarrera> findByCarrera(Carrera carrera);

    Optional<AnioCarrera> findByCarreraIdAndAnioId(
            Integer carreraId,
            Integer anioId
    );
}