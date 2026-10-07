package com.example.forupc_backend.controller;

import com.example.forupc_backend.modelo.Anio;
import com.example.forupc_backend.modelo.Carrera;
import com.example.forupc_backend.modelo.DuracionPublicacion;
import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.modelo.PublicacionRequest;
import com.example.forupc_backend.repository.AnioRepository;
import com.example.forupc_backend.repository.CarreraRepository;
import com.example.forupc_backend.repository.PublicacionRepository;
import com.example.forupc_backend.service.SupabaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final SupabaseStorageService storageService;

    public PublicacionController(
            PublicacionRepository publicacionRepository,
            CarreraRepository carreraRepository,
            AnioRepository anioRepository,
            SupabaseStorageService storageService
    ) {

        this.publicacionRepository = publicacionRepository;
        this.carreraRepository = carreraRepository;
        this.anioRepository = anioRepository;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<Publicacion>> listarPublicaciones() {

        return ResponseEntity.ok(
                publicacionRepository
                        .findAllByOrderByFechaDesc()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publicacion> obtenerPublicacion(
            @PathVariable Integer id
    ) {

        Optional<Publicacion> publicacion =
                publicacionRepository.findById(id);

        if (publicacion.isPresent()) {
            return ResponseEntity.ok(
                    publicacion.get()
            );
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> crearPublicacion(

            @RequestPart("datos")
            PublicacionRequest request,

            @RequestPart(
                    value = "imagen",
                    required = false
            )
            MultipartFile imagen,

            @RequestPart(
                    value = "archivo",
                    required = false
            )
            MultipartFile archivo
    ) {

        try {

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

            Publicacion publicacion =
                    new Publicacion();

            publicacion.setTitulo(
                    request.getTitulo()
            );

            publicacion.setMensaje(
                    request.getMensaje()
            );

            publicacion.setUrgente(
                    request.isUrgente()
            );

            LocalDateTime ahora =
                    LocalDateTime.now();

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
                        carreraRepository.findById(
                                request.getCarreraId()
                        );

                if (carrera.isEmpty()) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "La carrera indicada no existe."
                            );
                }

                publicacion.setCarrera(
                        carrera.get()
                );
            }

            if (request.getAnioId() != null) {

                Optional<Anio> anio =
                        anioRepository.findById(
                                request.getAnioId()
                        );

                if (anio.isEmpty()) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "El año indicado no existe."
                            );
                }

                publicacion.setAnio(
                        anio.get()
                );
            }

            if (imagen != null &&
                    !imagen.isEmpty()) {

                String imagenUrl =
                        storageService
                                .subirImagen(imagen);

                publicacion.setImagenUrl(
                        imagenUrl
                );
            }

            if (archivo != null &&
                    !archivo.isEmpty()) {

                SupabaseStorageService
                        .ArchivoSubido archivoSubido =
                        storageService
                                .subirDocumento(archivo);

                publicacion.setArchivoUrl(
                        archivoSubido.url()
                );

                publicacion.setArchivoNombre(
                        archivoSubido.nombre()
                );
            }

            Publicacion guardada =
                    publicacionRepository.save(
                            publicacion
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(guardada);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Error al crear la publicación: "
                                    + e.getMessage()
                    );
        }
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