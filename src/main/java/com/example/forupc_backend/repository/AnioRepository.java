package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.Anio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnioRepository extends JpaRepository<Anio, Integer> {

    Optional<Anio> findByNumero(Integer numero);
}