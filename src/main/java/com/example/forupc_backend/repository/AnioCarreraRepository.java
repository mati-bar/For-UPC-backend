package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnioCarreraRepository extends JpaRepository<AnioCarrera, Integer> {

    List<AnioCarrera> findByCarreraOrderByAnioNumeroAsc(Carrera carrera);

    Optional<AnioCarrera> findByCarreraIdAndAnioId(Integer carreraId, Integer anioId);

    @Query("SELECT ac FROM AnioCarrera ac JOIN FETCH ac.carrera JOIN FETCH ac.anio " +
            "ORDER BY ac.carrera.id ASC, ac.anio.numero ASC")
    List<AnioCarrera> findTodasConCarreraYAnio();
}
