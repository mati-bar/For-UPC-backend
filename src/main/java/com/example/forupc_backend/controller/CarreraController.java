package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Carrera;
import com.example.forupc_backend.repository.CarreraRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carreras")
@CrossOrigin(origins = "*")
public class CarreraController {

    private final CarreraRepository carreraRepository;

    public CarreraController(CarreraRepository carreraRepository) {
        this.carreraRepository = carreraRepository;
    }

    @GetMapping
    public List<Carrera> listar() {
        return carreraRepository.findAll();
    }

    @GetMapping("/{id}")
    public Carrera buscar(@PathVariable Integer id) {
        return carreraRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Carrera guardar(@RequestBody Carrera carrera) {
        return carreraRepository.save(carrera);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        carreraRepository.deleteById(id);
    }
}