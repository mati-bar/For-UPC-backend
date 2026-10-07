package com.example.forupc_backend.repository;

import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.modelo.PublicacionDestino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicacionDestinoRepository
        extends JpaRepository<PublicacionDestino, Integer> {

    Optional<PublicacionDestino>
    findByPublicacionAndAnioCarrera(
            Publicacion publicacion,
            AnioCarrera anioCarrera
    );
}