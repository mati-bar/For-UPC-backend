package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.Inscripcion;
import com.example.forupc_backend.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    List<Inscripcion> findByUsuario(Usuario usuario);
}