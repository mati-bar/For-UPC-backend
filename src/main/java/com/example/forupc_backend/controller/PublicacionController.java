
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

import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/publicaciones")
public class PublicacionController {

    private final PublicacionRepository publicacionRepository;
    private final CarreraRepository carreraRepository;
    private final AnioRepository anioRepository;
    private final SupabaseStorageService storageService;
    private final JsonMapper jsonMapper;

    public PublicacionController(
            PublicacionRepository publicacionRepository,
            CarreraRepository carreraRepository,
            AnioRepository anioRepository,
            SupabaseStorageService storageService,
            JsonMapper jsonMapper
    ) {
        this.publicacionRepository = publicacionRepository;
        this.carreraRepository = carreraRepository;
        this.anioRepository = anioRepository;
        this.storageService = storageService;
        this.jsonMapper = jsonMapper;
    }

    // =====================================
    // LISTAR PUBLICACIONES
    // =====================================

    @GetMapping
    public ResponseEntity<List<Publicacion>> listarPublicaciones() {
        return ResponseEntity.ok(
                publicacionRepository.findAllByOrderByFechaDesc()
        );
    }

    // =====================================
    // OBTENER PUBLICACION POR ID
    // =====================================

    @GetMapping("/{id}")
    public ResponseEntity<Publicacion> obtenerPublicacion(
            @PathVariable Integer id
    ) {
        return publicacionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =====================================
    // CREAR PUBLICACION
    // =====================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearPublicacion(

            @RequestPart("datos") String datos,

            @RequestPart(value = "imagen", required = false)
            MultipartFile imagen,

            @RequestPart(value = "archivo", required = false)
            MultipartFile archivo
    ) {
        try {

            PublicacionRequest request = convertirDatos(datos);

            String error = validarDatos(request);
            if (error != null) {
                return ResponseEntity.badRequest().body(error);
            }

            Publicacion publicacion = new Publicacion();

            publicacion.setFecha(LocalDateTime.now());

            aplicarDatos(publicacion, request, true);

            if (request.getCarreraId() != null &&
                    publicacion.getCarrera() == null) {

                return ResponseEntity.badRequest()
                        .body("La carrera indicada no existe.");
            }

            if (request.getAnioId() != null &&
                    publicacion.getAnio() == null) {

                return ResponseEntity.badRequest()
                        .body("El año indicado no existe.");
            }

            subirArchivos(publicacion, imagen, archivo);

            Publicacion guardada =
                    publicacionRepository.save(publicacion);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(guardada);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la publicación.");
        }
    }

    // =====================================
    // EDITAR PUBLICACION
    // SOLO ADMINISTRADOR (SECURITYCONFIG)
    // =====================================

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> editarPublicacion(

            @PathVariable Integer id,

            @RequestPart("datos") String datos,

            @RequestPart(value = "imagen", required = false)
            MultipartFile imagen,

            @RequestPart(value = "archivo", required = false)
            MultipartFile archivo
    ) {
        try {

            Optional<Publicacion> encontrada =
                    publicacionRepository.findById(id);

            if (encontrada.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PublicacionRequest request = convertirDatos(datos);

            String error = validarDatos(request);
            if (error != null) {
                return ResponseEntity.badRequest().body(error);
            }

            Publicacion publicacion = encontrada.get();

            // Validar las relaciones antes de modificar datos
            if (request.getCarreraId() != null &&
                    !carreraRepository.existsById(
                            request.getCarreraId())) {

                return ResponseEntity.badRequest()
                        .body("La carrera indicada no existe.");
            }

            if (request.getAnioId() != null &&
                    !anioRepository.existsById(
                            request.getAnioId())) {

                return ResponseEntity.badRequest()
                        .body("El año indicado no existe.");
            }

            // La fecha original no se modifica
            aplicarDatos(publicacion, request, false);

            // Si no se envían nuevos archivos,
            // se mantienen las URLs anteriores.
            subirArchivos(publicacion, imagen, archivo);

            Publicacion actualizada =
                    publicacionRepository.save(publicacion);

            return ResponseEntity.ok(actualizada);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al editar la publicación.");
        }
    }

    // =====================================
    // ELIMINAR PUBLICACION
    // =====================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPublicacion(
            @PathVariable Integer id
    ) {

        if (!publicacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        publicacionRepository.deleteById(id);

        return ResponseEntity.ok(
                "Publicación eliminada correctamente."
        );
    }

    // =====================================
    // CONVERTIR JSON
    // =====================================

    private PublicacionRequest convertirDatos(String datos) {

        if (datos == null || datos.isBlank()) {
            throw new IllegalArgumentException(
                    "Los datos de la publicación son obligatorios."
            );
        }

        try {

            PublicacionRequest request =
                    jsonMapper.readValue(
                            datos,
                            PublicacionRequest.class
                    );

            if (request == null) {
                throw new IllegalArgumentException(
                        "Los datos de la publicación son obligatorios."
                );
            }

            return request;

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "El campo datos no contiene un JSON válido."
            );
        }
    }

    // =====================================
    // VALIDACIONES
    // =====================================

    private String validarDatos(PublicacionRequest request) {

        if (request == null) {
            return "Los datos de la publicación son obligatorios.";
        }

        if (request.getTitulo() == null ||
                request.getTitulo().isBlank()) {

            return "El título es obligatorio.";
        }

        if (request.getMensaje() == null ||
                request.getMensaje().isBlank()) {

            return "El mensaje es obligatorio.";
        }

        return null;
    }

    // =====================================
    // APLICAR DATOS DE LA PUBLICACION
    // =====================================

    private void aplicarDatos(
            Publicacion publicacion,
            PublicacionRequest request,
            boolean creacion
    ) {

        publicacion.setTitulo(request.getTitulo().trim());
        publicacion.setMensaje(request.getMensaje());
        publicacion.setUrgente(request.isUrgente());

        // En creación, la fecha ya está establecida.
        // En edición, mantenemos esa fecha original.

        if (request.getDuracion() != null) {

            LocalDateTime referencia = LocalDateTime.now();

            publicacion.setFechaExpiracion(
                    calcularExpiracion(
                            referencia,
                            request.getDuracion()
                    )
            );

        } else if (creacion) {

            publicacion.setFechaExpiracion(null);
        }

        // Carrera: null significa sin carrera específica.
        if (request.getCarreraId() == null) {

            publicacion.setCarrera(null);

        } else {

            Carrera carrera = carreraRepository
                    .findById(request.getCarreraId())
                    .orElse(null);

            publicacion.setCarrera(carrera);
        }

        // Año: null significa sin año específico.
        if (request.getAnioId() == null) {

            publicacion.setAnio(null);

        } else {

            Anio anio = anioRepository
                    .findById(request.getAnioId())
                    .orElse(null);

            publicacion.setAnio(anio);
        }
    }

    // =====================================
    // CALCULAR FECHA DE EXPIRACION
    // =====================================

    private LocalDateTime calcularExpiracion(
            LocalDateTime referencia,
            DuracionPublicacion duracion
    ) {

        return switch (duracion) {

            case HORA -> referencia.plusHours(1);

            case DIA -> referencia.plusDays(1);

            case SEMANA -> referencia.plusWeeks(1);

            case SIN_VENCIMIENTO -> null;
        };
    }

    // =====================================
    // SUBIR O REEMPLAZAR ARCHIVOS
    // =====================================

    private void subirArchivos(
            Publicacion publicacion,
            MultipartFile imagen,
            MultipartFile archivo
    ) {

        if (imagen != null && !imagen.isEmpty()) {

            String imagenUrl =
                    storageService.subirImagen(imagen);

            publicacion.setImagenUrl(imagenUrl);
        }

        if (archivo != null && !archivo.isEmpty()) {

            SupabaseStorageService.ArchivoSubido subido =
                    storageService.subirDocumento(archivo);

            publicacion.setArchivoUrl(subido.url());
            publicacion.setArchivoNombre(subido.nombre());
        }
    }
}
