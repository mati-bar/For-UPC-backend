package com.example.forupc_backend.controller;

import com.example.forupc_backend.dto.CarreraRequest;
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


    // =========================
    // OBTENER TODAS LAS CARRERAS
    // =========================

    @GetMapping
    public ResponseEntity<List<Carrera>> obtenerTodas() {

        return ResponseEntity.ok(
                carreraRepository.findAll()
        );
    }


    // =========================
    // OBTENER CARRERA POR ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Integer id
    ) {

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


    // =========================
    // OBTENER AÑOS DE UNA CARRERA
    // =========================

    @GetMapping("/{id}/anios")
    public ResponseEntity<?> obtenerAnios(
            @PathVariable Integer id
    ) {

        Carrera carrera = carreraRepository
                .findById(id)
                .orElse(null);

        if (carrera == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        List<AnioCarrera> relaciones =
                anioCarreraRepository
                        .findByCarreraOrderByAnioNumeroAsc(carrera);

        List<Anio> anios = new ArrayList<>();

        for (AnioCarrera relacion : relaciones) {
            anios.add(relacion.getAnio());
        }

        return ResponseEntity.ok(anios);
    }


    // =========================
    // CREAR CARRERA
    // =========================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody CarreraRequest datos
    ) {

        if (datos.getNombre() == null ||
                datos.getNombre().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "El nombre de la carrera es obligatorio"
                    );
        }

        if (datos.getCantidadAnios() == null ||
                datos.getCantidadAnios() < 1 ||
                datos.getCantidadAnios() > 4) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "La cantidad de años debe estar entre 1 y 4"
                    );
        }


        // Verificar nombre duplicado
        for (Carrera carreraExistente :
                carreraRepository.findAll()) {

            if (carreraExistente.getNombre()
                    .equalsIgnoreCase(
                            datos.getNombre().trim()
                    )) {

                return ResponseEntity
                        .status(409)
                        .body(
                                "Ya existe una carrera con ese nombre"
                        );
            }
        }


        // Crear carrera
        Carrera carrera = new Carrera(
                datos.getNombre().trim()
        );

        Carrera carreraGuardada =
                carreraRepository.save(carrera);


        // Crear años automáticamente
        for (int numero = 1;
             numero <= datos.getCantidadAnios();
             numero++) {

            Anio anio = anioRepository
                    .findByNumero(numero)
                    .orElse(null);

            if (anio == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "No existe el año " +
                                        numero +
                                        " en la base de datos"
                        );
            }

            AnioCarrera anioCarrera =
                    new AnioCarrera(
                            carreraGuardada,
                            anio
                    );

            anioCarreraRepository.save(
                    anioCarrera
            );
        }


        return ResponseEntity
                .status(201)
                .body(carreraGuardada);
    }


    // =========================
    // EDITAR CARRERA
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable Integer id,
            @RequestBody CarreraRequest datos
    ) {

        Carrera carrera = carreraRepository
                .findById(id)
                .orElse(null);

        if (carrera == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }


        // Verificar nombre
        if (datos.getNombre() == null ||
                datos.getNombre().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "El nombre de la carrera es obligatorio"
                    );
        }


        // Verificar cantidad
        if (datos.getCantidadAnios() == null ||
                datos.getCantidadAnios() < 1 ||
                datos.getCantidadAnios() > 4) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "La cantidad de años debe estar entre 1 y 4"
                    );
        }


        // Verificar que el nuevo nombre
        // no pertenezca a otra carrera
        for (Carrera carreraExistente :
                carreraRepository.findAll()) {

            if (!carreraExistente.getId()
                    .equals(id) &&
                    carreraExistente.getNombre()
                            .equalsIgnoreCase(
                                    datos.getNombre().trim()
                            )) {

                return ResponseEntity
                        .status(409)
                        .body(
                                "Ya existe otra carrera con ese nombre"
                        );
            }
        }


        // Cambiar nombre
        carrera.setNombre(
                datos.getNombre().trim()
        );

        carreraRepository.save(carrera);


        // Obtener relaciones actuales
        List<AnioCarrera> relacionesActuales =
                anioCarreraRepository
                        .findByCarreraOrderByAnioNumeroAsc(
                                carrera
                        );


        // Agregar los años que falten
        for (int numero = 1;
             numero <= datos.getCantidadAnios();
             numero++) {

            boolean existe = false;

            for (AnioCarrera relacion :
                    relacionesActuales) {

                if (relacion.getAnio()
                        .getNumero()
                        .equals(numero)) {

                    existe = true;
                    break;
                }
            }


            if (!existe) {

                Anio anio = anioRepository
                        .findByNumero(numero)
                        .orElse(null);

                if (anio == null) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "No existe el año " +
                                            numero +
                                            " en la base de datos"
                            );
                }


                AnioCarrera nuevaRelacion =
                        new AnioCarrera(
                                carrera,
                                anio
                        );

                anioCarreraRepository.save(
                        nuevaRelacion
                );
            }
        }


        // Eliminar años que ya no correspondan
        for (AnioCarrera relacion :
                relacionesActuales) {

            if (relacion.getAnio()
                    .getNumero()
                    > datos.getCantidadAnios()) {

                anioCarreraRepository.delete(
                        relacion
                );
            }
        }


        return ResponseEntity.ok(
                carrera
        );
    }


    // =========================
    // ELIMINAR CARRERA
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id
    ) {

        Carrera carrera = carreraRepository
                .findById(id)
                .orElse(null);

        if (carrera == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // Buscar relaciones carrera + año
        List<AnioCarrera> relaciones =
                anioCarreraRepository
                        .findByCarreraOrderByAnioNumeroAsc(
                                carrera
                        );


        // Eliminar relaciones
        for (AnioCarrera relacion : relaciones) {

            anioCarreraRepository.delete(
                    relacion
            );
        }


        // Eliminar carrera
        carreraRepository.delete(
                carrera
        );


        return ResponseEntity.ok(
                "Carrera eliminada correctamente"
        );
    }
}