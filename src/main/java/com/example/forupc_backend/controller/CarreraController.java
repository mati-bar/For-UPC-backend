package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Carrera;
import com.example.forupc_backend.repository.CarreraRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carreras")
public class CarreraController {

    private final CarreraRepository carreraRepository;

    public CarreraController(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    // Obtener todas las carreras
    @GetMapping
    public ResponseEntity<List<Carrera>> obtenerTodas() {
        return ResponseEntity.ok(carreraRepository.findAll());
    }

    // Obtener una carrera por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {

        Carrera carrera = carreraRepository
                .findById(id)
                .orElse(null);

        if (carrera == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(carrera);
    }

    // Crear una carrera
    @PostMapping
    public ResponseEntity<Carrera> crear(
            @RequestBody Carrera carrera
    ) {
        Carrera nueva = carreraRepository.save(carrera);

        return ResponseEntity
                .status(201)
                .body(nueva);
    }

    // Eliminar una carrera
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {

        if (!carreraRepository.existsById(id)) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        carreraRepository.deleteById(id);

        return ResponseEntity.ok(
                "Carrera eliminada correctamente"
        );
    }
}