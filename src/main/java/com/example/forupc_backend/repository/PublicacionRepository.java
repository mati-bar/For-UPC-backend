package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
}