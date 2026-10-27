package com.example.forupc_backend.controller;

import com.example.forupc_backend.dto.CarreraRequest;
import com.example.forupc_backend.dto.AnioCarreraResponse;
import com.example.forupc_backend.dto.CarreraConAniosResponse;

import com.example.forupc_backend.modelo.Anio;
import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Carrera;

import com.example.forupc_backend.repository.AnioCarreraRepository;
import com.example.forupc_backend.repository.AnioRepository;
import com.example.forupc_backend.repository.CarreraRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

@RestController
@RequestMapping("/carreras")
public class CarreraController {

    private final CarreraRepository carreraRepository;
    private final AnioRepository anioRepository;
    private final AnioCarreraRepository anioCarreraRepository;

    public CarreraController(
            CarreraRepository carreraRepository,
            AnioRepository anioRepository,
            AnioCarreraRepository anioCarreraRepository
    ) {
        this.carreraRepository = carreraRepository;
        this.anioRepository = anioRepository;
        this.anioCarreraRepository = anioCarreraRepository;
    }

    // =====================================
    // OBTENER TODAS LAS CARRERAS
    // =====================================
    @GetMapping
    public ResponseEntity<List<Carrera>> obtenerTodas() {
        return ResponseEntity.ok(carreraRepository.findAll());
    }

    // =====================================
    // OBTENER TODAS LAS CARRERAS CON SUS ANIOS
    // UNA SOLA SOLICITUD DESDE EL FRONTEND
    // =====================================
    @GetMapping("/con-anios")
    public ResponseEntity<List<CarreraConAniosResponse>> obtenerTodasConAnios() {
        List<Carrera> carreras = carreraRepository.findAll();
        carreras.sort(Comparator.comparing(Carrera::getNombre, String.CASE_INSENSITIVE_ORDER));

        // Una consulta obtiene TODAS las relaciones junto con sus anios.
        List<AnioCarrera> relaciones = anioCarreraRepository.findTodasConCarreraYAnio();

        Map<Integer, List<AnioCarreraResponse>> aniosPorCarrera = new HashMap<>();
        for (AnioCarrera relacion : relaciones) {
            Integer carreraId = relacion.getCarrera().getId();
            aniosPorCarrera.computeIfAbsent(carreraId, key -> new ArrayList<>())
                    .add(new AnioCarreraResponse(
                            relacion.getId(),
                            relacion.getAnio().getNumero()
                    ));
        }

        List<CarreraConAniosResponse> respuesta = new ArrayList<>();
        for (Carrera carrera : carreras) {
            respuesta.add(new CarreraConAniosResponse(
                    carrera.getId(),
                    carrera.getNombre(),
                    aniosPorCarrera.getOrDefault(carrera.getId(), List.of())
            ));
        }
        return ResponseEntity.ok(respuesta);
    }

    // =====================================
    // OBTENER CARRERA POR ID
    // =====================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        Carrera carrera = carreraRepository.findById(id).orElse(null);
        if (carrera == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carrera);
    }

    // =====================================
    // OBTENER ANIOS DE UNA CARRERA
    // =====================================
    @GetMapping("/{id}/anios")
    public ResponseEntity<?> obtenerAnios(@PathVariable Integer id) {
        Carrera carrera = carreraRepository.findById(id).orElse(null);
        if (carrera == null) {
            return ResponseEntity.notFound().build();
        }

        List<AnioCarrera> relaciones =
                anioCarreraRepository.findByCarreraOrderByAnioNumeroAsc(carrera);

        List<AnioCarreraResponse> respuesta = new ArrayList<>();
        for (AnioCarrera relacion : relaciones) {
            respuesta.add(new AnioCarreraResponse(
                    relacion.getId(),
                    relacion.getAnio().getNumero()
            ));
        }
        return ResponseEntity.ok(respuesta);
    }

    // =====================================
    // CREAR CARRERA
    // =====================================
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CarreraRequest datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre de la carrera es obligatorio");
        }
        if (datos.getCantidadAnios() == null || datos.getCantidadAnios() < 1 || datos.getCantidadAnios() > 4) {
            return ResponseEntity.badRequest().body("La cantidad de años debe estar entre 1 y 4");
        }

        for (Carrera carreraExistente : carreraRepository.findAll()) {
            if (carreraExistente.getNombre().equalsIgnoreCase(datos.getNombre().trim())) {
                return ResponseEntity.status(409).body("Ya existe una carrera con ese nombre");
            }
        }

        Carrera carrera = new Carrera(datos.getNombre().trim());
        Carrera carreraGuardada = carreraRepository.save(carrera);

        for (int numero = 1; numero <= datos.getCantidadAnios(); numero++) {
            Anio anio = anioRepository.findByNumero(numero).orElse(null);
            if (anio == null) {
                return ResponseEntity.badRequest().body(
                        "No existe el año " + numero + " en la base de datos");
            }
            anioCarreraRepository.save(new AnioCarrera(carreraGuardada, anio));
        }
        return ResponseEntity.status(201).body(carreraGuardada);
    }

    // =====================================
    // EDITAR CARRERA
    // =====================================
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody CarreraRequest datos) {
        Carrera carrera = carreraRepository.findById(id).orElse(null);
        if (carrera == null) {
            return ResponseEntity.notFound().build();
        }
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre de la carrera es obligatorio");
        }
        if (datos.getCantidadAnios() == null || datos.getCantidadAnios() < 1 || datos.getCantidadAnios() > 4) {
            return ResponseEntity.badRequest().body("La cantidad de años debe estar entre 1 y 4");
        }

        for (Carrera carreraExistente : carreraRepository.findAll()) {
            if (!carreraExistente.getId().equals(id) &&
                    carreraExistente.getNombre().equalsIgnoreCase(datos.getNombre().trim())) {
                return ResponseEntity.status(409).body("Ya existe otra carrera con ese nombre");
            }
        }

        carrera.setNombre(datos.getNombre().trim());
        carreraRepository.save(carrera);
        List<AnioCarrera> relacionesActuales =
                anioCarreraRepository.findByCarreraOrderByAnioNumeroAsc(carrera);

        for (int numero = 1; numero <= datos.getCantidadAnios(); numero++) {
            boolean existe = false;
            for (AnioCarrera relacion : relacionesActuales) {
                if (relacion.getAnio().getNumero().equals(numero)) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                Anio anio = anioRepository.findByNumero(numero).orElse(null);
                if (anio == null) {
                    return ResponseEntity.badRequest().body(
                            "No existe el año " + numero + " en la base de datos");
                }
                anioCarreraRepository.save(new AnioCarrera(carrera, anio));
            }
        }

        for (AnioCarrera relacion : relacionesActuales) {
            if (relacion.getAnio().getNumero() > datos.getCantidadAnios()) {
                anioCarreraRepository.delete(relacion);
            }
        }
        return ResponseEntity.ok(carrera);
    }

    // =====================================
    // ELIMINAR CARRERA
    // =====================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        Carrera carrera = carreraRepository.findById(id).orElse(null);
        if (carrera == null) {
            return ResponseEntity.notFound().build();
        }
        List<AnioCarrera> relaciones =
                anioCarreraRepository.findByCarreraOrderByAnioNumeroAsc(carrera);
        for (AnioCarrera relacion : relaciones) {
            anioCarreraRepository.delete(relacion);
        }
        carreraRepository.delete(carrera);
        return ResponseEntity.ok("Carrera eliminada correctamente");
    }
}
