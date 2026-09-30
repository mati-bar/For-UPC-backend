package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Anio;
import com.example.forupc_backend.modelo.Carrera;
import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.modelo.PublicacionDestino;
import com.example.forupc_backend.repository.AnioRepository;
import com.example.forupc_backend.repository.CarreraRepository;
import com.example.forupc_backend.repository.PublicacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publicaciones")
public class PublicacionController {

    private final PublicacionRepository publicacionRepository;
    private final CarreraRepository carreraRepository;
    private final AnioRepository anioRepository;

    public PublicacionController(
            PublicacionRepository publicacionRepository,
            CarreraRepository carreraRepository,
            AnioRepository anioRepository
    ) {
        this.publicacionRepository = publicacionRepository;
        this.carreraRepository = carreraRepository;
        this.anioRepository = anioRepository;
    }

    // =========================
    // OBTENER TODAS
    // =========================

    @GetMapping
    public ResponseEntity<List<Publicacion>> obtenerTodas() {

        return ResponseEntity.ok(
                publicacionRepository.findAll()
        );
    }

    // =========================
    // OBTENER POR ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Integer id
    ) {

        Publicacion publicacion = publicacionRepository
                .findById(id)
                .orElse(null);

        if (publicacion == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(publicacion);
    }

    // =========================
    // CREAR PUBLICACION
    // =========================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Publicacion publicacion
    ) {

        if (publicacion.getTitulo() == null ||
                publicacion.getTitulo().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("El título es obligatorio");
        }

        if (publicacion.getContenido() == null ||
                publicacion.getContenido().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("El contenido es obligatorio");
        }

        Publicacion nueva = publicacionRepository.save(publicacion);

        return ResponseEntity
                .status(201)
                .body(nueva);
    }

    // =========================
    // ELIMINAR
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id
    ) {

        if (!publicacionRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        publicacionRepository.deleteById(id);

        return ResponseEntity.ok(
                "Publicación eliminada correctamente"
        );
    }

    // =========================
    // AGREGAR DESTINO
    // =========================

    @PostMapping("/{publicacionId}/destinos")
    public ResponseEntity<?> agregarDestino(
            @PathVariable Integer publicacionId,
            @RequestParam Integer carreraId,
            @RequestParam Integer anioId
    ) {

        Publicacion publicacion = publicacionRepository
                .findById(publicacionId)
                .orElse(null);

        if (publicacion == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        Carrera carrera = carreraRepository
                .findById(carreraId)
                .orElse(null);

        if (carrera == null) {

            return ResponseEntity
                    .badRequest()
                    .body("La carrera no existe");
        }

        Anio anio = anioRepository
                .findById(anioId)
                .orElse(null);

        if (anio == null) {

            return ResponseEntity
                    .badRequest()
                    .body("El año no existe");
        }

        PublicacionDestino destino = new PublicacionDestino(
                publicacion,
                carrera,
                anio
        );

        publicacion.agregarDestino(destino);

        publicacionRepository.save(publicacion);

        return ResponseEntity
                .status(201)
                .body(destino);
    }
}