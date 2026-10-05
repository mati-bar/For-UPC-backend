package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Anio;
import com.example.forupc_backend.repository.AnioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anios")
public class AnioController {

    private final AnioRepository anioRepository;

    public AnioController(
            AnioRepository anioRepository
    ) {
        this.anioRepository = anioRepository;
    }


    // =========================
    // OBTENER TODOS LOS AÑOS
    // =========================

    @GetMapping
    public ResponseEntity<List<Anio>> obtenerTodos() {

        return ResponseEntity.ok(
                anioRepository.findAll()
        );
    }


    // =========================
    // OBTENER AÑO POR ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Integer id
    ) {

        Anio anio = anioRepository
                .findById(id)
                .orElse(null);

        if (anio == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(anio);
    }


    // =========================
    // CREAR AÑO
    // =========================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Anio anio
    ) {

        if (anio.getNumero() == null ||
                anio.getNumero() < 1 ||
                anio.getNumero() > 4) {

            return ResponseEntity
                    .badRequest()
                    .body("El año debe estar entre 1 y 4");
        }

        if (anioRepository.findByNumero(anio.getNumero()).isPresent()) {

            return ResponseEntity
                    .status(409)
                    .body("Ese año ya existe");
        }

        Anio nuevo = anioRepository.save(anio);

        return ResponseEntity
                .status(201)
                .body(nuevo);
    }


    // =========================
    // ELIMINAR AÑO
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id
    ) {

        if (!anioRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        anioRepository.deleteById(id);

        return ResponseEntity.ok(
                "Año eliminado correctamente"
        );
    }
}