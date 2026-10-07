package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    List<Publicacion> findAllByOrderByFechaDesc();

    List<Publicacion> findByFechaExpiracionBefore(LocalDateTime fecha);

}