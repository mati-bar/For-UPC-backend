package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Anio;
import com.example.forupc_backend.repository.AnioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anios")
@CrossOrigin(origins = "*")
public class AnioController {

    private final AnioRepository anioRepository;

    public AnioController(AnioRepository anioRepository) {
        this.anioRepository = anioRepository;
    }

    @GetMapping
    public List<Anio> listar() {
        return anioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Anio buscar(@PathVariable Integer id) {
        return anioRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Anio guardar(@RequestBody Anio anio) {
        return anioRepository.save(anio);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        anioRepository.deleteById(id);
    }
}