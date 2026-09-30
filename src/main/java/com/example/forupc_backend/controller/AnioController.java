package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Anio;
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

    public AnioController(AnioRepository anioRepository) {
        this.anioRepository = anioRepository;
    }

    // Obtener todos los años
    @GetMapping
    public ResponseEntity<List<Anio>> obtenerTodos() {
        return ResponseEntity.ok(anioRepository.findAll());
    }

    // Obtener un año por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {

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

    // Crear un año
    @PostMapping
    public ResponseEntity<Anio> crear(
            @RequestBody Anio anio
    ) {
        Anio nuevo = anioRepository.save(anio);

        return ResponseEntity
                .status(201)
                .body(nuevo);
    }

    // Eliminar un año
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {

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