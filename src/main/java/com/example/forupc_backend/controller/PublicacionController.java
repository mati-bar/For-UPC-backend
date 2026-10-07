package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Carrera;
import com.example.forupc_backend.modelo.Anio;
import com.example.forupc_backend.modelo.DuracionPublicacion;
import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.modelo.PublicacionRequest;
import com.example.forupc_backend.repository.AnioRepository;
import com.example.forupc_backend.repository.CarreraRepository;
import com.example.forupc_backend.repository.PublicacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/publicaciones")
@CrossOrigin(origins = "*")
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

    @GetMapping
    public ResponseEntity<List<Publicacion>> listarPublicaciones() {

        return ResponseEntity.ok(
                publicacionRepository.findAllByOrderByFechaDesc()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publicacion> obtenerPublicacion(
            @PathVariable Integer id
    ) {

        Optional<Publicacion> publicacion =
                publicacionRepository.findById(id);

        if (publicacion.isPresent()) {
            return ResponseEntity.ok(publicacion.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crearPublicacion(
            @RequestBody PublicacionRequest request
    ) {


        if (request.getTitulo() == null ||
                request.getTitulo().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("El título es obligatorio.");
        }

        if (request.getMensaje() == null ||
                request.getMensaje().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("El mensaje es obligatorio.");
        }

        Publicacion publicacion = new Publicacion();

        publicacion.setTitulo(request.getTitulo());
        publicacion.setMensaje(request.getMensaje());

        LocalDateTime ahora = LocalDateTime.now();

        publicacion.setFecha(ahora);

        if (request.getDuracion() == null) {

            publicacion.setFechaExpiracion(null);

        } else {

            switch (request.getDuracion()) {

                case HORA:
                    publicacion.setFechaExpiracion(
                            ahora.plusHours(1)
                    );
                    break;

                case DIA:
                    publicacion.setFechaExpiracion(
                            ahora.plusDays(1)
                    );
                    break;

                case SEMANA:
                    publicacion.setFechaExpiracion(
                            ahora.plusWeeks(1)
                    );
                    break;

                case SIN_VENCIMIENTO:
                    publicacion.setFechaExpiracion(null);
                    break;
            }
        }

        if (request.getCarreraId() != null) {

            Optional<Carrera> carrera =
                    carreraRepository.findById(request.getCarreraId());

            if (carrera.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("La carrera indicada no existe.");
            }

            publicacion.setCarrera(carrera.get());
        }

        if (request.getAnioId() != null) {

            Optional<Anio> anio =
                    anioRepository.findById(request.getAnioId());

            if (anio.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("El año indicado no existe.");
            }

            publicacion.setAnio(anio.get());
        }

        Publicacion guardada =
                publicacionRepository.save(publicacion);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPublicacion(
            @PathVariable Integer id
    ) {

        if (!publicacionRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        publicacionRepository.deleteById(id);

        return ResponseEntity.ok(
                "Publicación eliminada correctamente."
        );
    }
}