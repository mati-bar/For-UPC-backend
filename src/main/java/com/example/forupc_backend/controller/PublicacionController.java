package com.example.forupc_backend.controller;

import com.example.forupc_backend.dto.PublicacionRequest;
import com.example.forupc_backend.dto.PublicacionesPaginadasResponse;
import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.DuracionPublicacion;
import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.modelo.PublicacionDestino;
import com.example.forupc_backend.repository.AnioCarreraRepository;
import com.example.forupc_backend.repository.PublicacionRepository;
import com.example.forupc_backend.service.SupabaseStorageService;
import com.example.forupc_backend.service.PublicacionConsultaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/publicaciones")
public class PublicacionController {

    private final PublicacionRepository publicacionRepository;
    private final AnioCarreraRepository anioCarreraRepository;
    private final SupabaseStorageService storageService;
    private final JsonMapper jsonMapper;
    private final PublicacionConsultaService publicacionConsultaService;

    public PublicacionController(
            PublicacionRepository publicacionRepository,
            AnioCarreraRepository anioCarreraRepository,
            SupabaseStorageService storageService,
            JsonMapper jsonMapper,
            PublicacionConsultaService publicacionConsultaService
    ) {
        this.publicacionRepository = publicacionRepository;
        this.anioCarreraRepository = anioCarreraRepository;
        this.storageService = storageService;
        this.jsonMapper = jsonMapper;
        this.publicacionConsultaService = publicacionConsultaService;
    }

    // =====================================
    // LISTAR PUBLICACIONES PAGINADAS
    // =====================================

    @GetMapping
    public ResponseEntity<PublicacionesPaginadasResponse> listarPublicaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }

        PublicacionesPaginadasResponse respuesta =
                publicacionConsultaService.listarPublicaciones(page, size);

        return ResponseEntity.ok(respuesta);
    }

    // =====================================
    // OBTENER PUBLICACION POR ID
    // =====================================

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPublicacion(@PathVariable Integer id) {
        return publicacionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =====================================
    // CREAR PUBLICACION (SIN CAMBIOS)
    // =====================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearPublicacion(
            @RequestPart("datos") String datos,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) {
        try {
            PublicacionRequest request = convertirDatos(datos);
            validarDatos(request);
            List<AnioCarrera> relaciones = validarInscripciones(request);

            Publicacion publicacion = new Publicacion();
            publicacion.setFecha(LocalDateTime.now());
            aplicarDatos(publicacion, request, true);
            asignarDestinos(publicacion, relaciones);
            subirArchivos(publicacion, imagen, archivo);

            Publicacion guardada = publicacionRepository.saveAndFlush(publicacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la publicación.");
        }
    }

    // =====================================
    // EDITAR PUBLICACION - CORREGIDO
    // =====================================

    @Transactional
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editarPublicacion(
            @PathVariable Integer id,
            @RequestPart("datos") String datos,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) {
        try {
            // La transacción permanece abierta mientras se accede a destinos LAZY.
            Publicacion publicacion = publicacionRepository.findById(id).orElse(null);
            if (publicacion == null) {
                return ResponseEntity.notFound().build();
            }

            PublicacionRequest request = convertirDatos(datos);
            validarDatos(request);
            List<AnioCarrera> relaciones = validarInscripciones(request);

            aplicarDatos(publicacion, request, false);
            asignarDestinos(publicacion, relaciones);
            subirArchivos(publicacion, imagen, archivo);

            Publicacion actualizada = publicacionRepository.saveAndFlush(publicacion);

            // Construir DTO antes de cerrar la sesión para no serializar entidades LAZY.
            return ResponseEntity.ok(convertirPublicacion(actualizada));

        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al editar la publicación.");
        }
    }

    // =====================================
    // ELIMINAR PUBLICACION
    // =====================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPublicacion(@PathVariable Integer id) {
        if (!publicacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        publicacionRepository.deleteById(id);
        return ResponseEntity.ok("Publicación eliminada correctamente.");
    }

    // =====================================
    // DTO DE RESPUESTA DE EDICION
    // =====================================

    private PublicacionesPaginadasResponse.PublicacionItem convertirPublicacion(
            Publicacion publicacion
    ) {
        List<PublicacionesPaginadasResponse.DestinoItem> destinos =
                publicacion.getDestinos().stream()
                        .map(destino -> new PublicacionesPaginadasResponse.DestinoItem(
                                destino.getId(),
                                new PublicacionesPaginadasResponse.CarreraItem(
                                        destino.getCarrera().getId(),
                                        destino.getCarrera().getNombre()
                                ),
                                new PublicacionesPaginadasResponse.AnioItem(
                                        destino.getAnio().getId(),
                                        destino.getAnio().getNumero()
                                )
                        ))
                        .toList();

        return new PublicacionesPaginadasResponse.PublicacionItem(
                publicacion.getId(),
                publicacion.getTitulo(),
                publicacion.getMensaje(),
                publicacion.getFecha(),
                publicacion.getFechaExpiracion(),
                publicacion.isUrgente(),
                publicacion.getImagenUrl(),
                publicacion.getArchivoUrl(),
                publicacion.getArchivoNombre(),
                publicacion.getCarrera() != null ? publicacion.getCarrera().getId() : null,
                publicacion.getAnio() != null ? publicacion.getAnio().getId() : null,
                destinos
        );
    }

    // =====================================
    // CONVERTIR JSON
    // =====================================

    private PublicacionRequest convertirDatos(String datos) {
        if (datos == null || datos.isBlank()) {
            throw new IllegalArgumentException("Los datos de la publicación son obligatorios.");
        }
        try {
            PublicacionRequest request = jsonMapper.readValue(datos, PublicacionRequest.class);
            if (request == null) {
                throw new IllegalArgumentException("Los datos de la publicación son obligatorios.");
            }
            return request;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("El campo datos no contiene un JSON válido.");
        }
    }

    // =====================================
    // VALIDAR DATOS GENERALES
    // =====================================

    private void validarDatos(PublicacionRequest request) {
        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (request.getMensaje() == null || request.getMensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio.");
        }
    }

    // =====================================
    // VALIDAR INSCRIPCIONES MULTIPLES
    // =====================================

    private List<AnioCarrera> validarInscripciones(PublicacionRequest request) {
        if (request.getInscripciones() == null || request.getInscripciones().isEmpty()) {
            throw new IllegalArgumentException("La publicación debe tener al menos un destino.");
        }
        Set<Integer> idsSeleccionados = new HashSet<>();
        List<AnioCarrera> relaciones = new ArrayList<>();

        for (PublicacionRequest.DestinoRequest destino : request.getInscripciones()) {
            if (destino == null || destino.getAnioCarreraId() == null) {
                throw new IllegalArgumentException("Cada destino debe indicar un anioCarreraId.");
            }
            Integer id = destino.getAnioCarreraId();
            if (!idsSeleccionados.add(id)) {
                throw new IllegalArgumentException(
                        "No podés repetir la misma combinación de carrera y año.");
            }
            AnioCarrera relacion = anioCarreraRepository.findById(id).orElse(null);
            if (relacion == null) {
                throw new IllegalArgumentException(
                        "La combinación de carrera y año indicada no existe.");
            }
            relaciones.add(relacion);
        }
        return relaciones;
    }

    // =====================================
    // ASIGNAR DESTINOS SIN DUPLICADOS
    // =====================================

    private void asignarDestinos(Publicacion publicacion, List<AnioCarrera> relaciones) {
        Set<String> combinacionesSolicitadas = new HashSet<>();
        for (AnioCarrera relacion : relaciones) {
            String clave = relacion.getCarrera().getId() + "-" + relacion.getAnio().getId();
            combinacionesSolicitadas.add(clave);
        }

        // En edición, destinos se inicializa dentro de la transacción activa.
        publicacion.getDestinos().removeIf(destino -> {
            String clave = destino.getCarrera().getId() + "-" + destino.getAnio().getId();
            return !combinacionesSolicitadas.contains(clave);
        });

        Set<String> combinacionesExistentes = new HashSet<>();
        for (PublicacionDestino destino : publicacion.getDestinos()) {
            String clave = destino.getCarrera().getId() + "-" + destino.getAnio().getId();
            combinacionesExistentes.add(clave);
        }

        for (AnioCarrera relacion : relaciones) {
            String clave = relacion.getCarrera().getId() + "-" + relacion.getAnio().getId();
            if (!combinacionesExistentes.contains(clave)) {
                PublicacionDestino destino = new PublicacionDestino(
                        publicacion, relacion.getCarrera(), relacion.getAnio());
                publicacion.getDestinos().add(destino);
                combinacionesExistentes.add(clave);
            }
        }
        publicacion.setCarrera(null);
        publicacion.setAnio(null);
    }

    // =====================================
    // APLICAR DATOS GENERALES
    // =====================================

    private void aplicarDatos(Publicacion publicacion, PublicacionRequest request, boolean creacion) {
        publicacion.setTitulo(request.getTitulo().trim());
        publicacion.setMensaje(request.getMensaje());
        publicacion.setUrgente(request.isUrgente());

        if (request.getDuracion() != null) {
            publicacion.setFechaExpiracion(
                    calcularExpiracion(LocalDateTime.now(), request.getDuracion()));
        } else if (creacion) {
            publicacion.setFechaExpiracion(null);
        }
    }

    private LocalDateTime calcularExpiracion(
            LocalDateTime referencia, DuracionPublicacion duracion) {
        return switch (duracion) {
            case HORA -> referencia.plusHours(1);
            case DIA -> referencia.plusDays(1);
            case SEMANA -> referencia.plusWeeks(1);
            case SIN_VENCIMIENTO -> null;
        };
    }

    // =====================================
    // SUBIR O CONSERVAR ARCHIVOS
    // =====================================

    private void subirArchivos(
            Publicacion publicacion, MultipartFile imagen, MultipartFile archivo) {
        if (imagen != null && !imagen.isEmpty()) {
            String imagenUrl = storageService.subirImagen(imagen);
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
