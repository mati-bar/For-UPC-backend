package com.example.forupc_backend.controller;

import com.example.forupc_backend.dto.*;
import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Inscripcion;
import com.example.forupc_backend.modelo.Usuario;
import com.example.forupc_backend.repository.AnioCarreraRepository;
import com.example.forupc_backend.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AnioCarreraRepository anioCarreraRepository;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            AnioCarreraRepository anioCarreraRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.anioCarreraRepository = anioCarreraRepository;
    }

    // =====================================
    // CONSULTAR MI PERFIL
    // =====================================

    @GetMapping("/me")
    @Transactional
    public ResponseEntity<?> obtenerMiPerfil(
            Authentication authentication
    ) {

        Usuario usuario = buscarUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body("Usuario no autenticado o inexistente.");
        }

        return ResponseEntity.ok(construirPerfil(usuario));
    }

    // =====================================
    // ACTUALIZAR DATOS PERSONALES
    // =====================================

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<?> actualizarMiPerfil(
            Authentication authentication,
            @RequestBody ActualizarPerfilRequest datos
    ) {

        Usuario usuario = buscarUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body("Usuario no autenticado o inexistente.");
        }

        if (datos == null ||
                datos.nombre() == null ||
                datos.nombre().isBlank() ||
                datos.apellido() == null ||
                datos.apellido().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El nombre y el apellido son obligatorios.");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellido(datos.apellido().trim());

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(construirPerfil(usuario));
    }

    // =====================================
    // ACTUALIZAR MIS INSCRIPCIONES
    // =====================================

    @PutMapping("/me/inscripciones")
    @Transactional
    public ResponseEntity<?> actualizarInscripciones(
            Authentication authentication,
            @RequestBody ActualizarInscripcionesRequest datos
    ) {

        Usuario usuario = buscarUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body("Usuario no autenticado o inexistente.");
        }

        if (!usuario.getRol().name().equals("ESTUDIANTE")) {
            return ResponseEntity.status(403)
                    .body("Esta operación es solo para estudiantes.");
        }

        if (datos == null ||
                datos.anioCarreraIds() == null ||
                datos.anioCarreraIds().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Debés seleccionar al menos una inscripción.");
        }

        Set<Integer> carreras = new HashSet<>();
        Set<Integer> idsSolicitados = new HashSet<>();
        List<AnioCarrera> nuevasRelaciones = new ArrayList<>();

        // Validar todos los datos antes de modificar la BD.
        for (Integer id : datos.anioCarreraIds()) {

            if (id == null) {
                return ResponseEntity.badRequest()
                        .body("El identificador de inscripción es obligatorio.");
            }

            AnioCarrera relacion = anioCarreraRepository
                    .findById(id)
                    .orElse(null);

            if (relacion == null) {
                return ResponseEntity.badRequest()
                        .body("La combinación de carrera y año no existe.");
            }

            Integer carreraId = relacion.getCarrera().getId();

            if (!carreras.add(carreraId)) {
                return ResponseEntity.badRequest()
                        .body("No podés seleccionar dos años de la misma carrera.");
            }

            idsSolicitados.add(id);
            nuevasRelaciones.add(relacion);
        }

        // Quitar únicamente las inscripciones que ya
        // no están en la selección enviada.
        List<Inscripcion> actuales =
                new ArrayList<>(usuario.getInscripciones());

        for (Inscripcion inscripcion : actuales) {

            Integer idActual =
                    inscripcion.getAnioCarrera().getId();

            if (!idsSolicitados.contains(idActual)) {
                usuario.eliminarInscripcion(inscripcion);
            }
        }

        // Ejecutar las bajas antes de agregar nuevas
        // relaciones, respetando restricciones únicas.
        usuarioRepository.saveAndFlush(usuario);

        Set<Integer> idsExistentes = new HashSet<>();

        for (Inscripcion inscripcion : usuario.getInscripciones()) {
            idsExistentes.add(
                    inscripcion.getAnioCarrera().getId()
            );
        }

        // Agregar únicamente las relaciones faltantes.
        for (AnioCarrera relacion : nuevasRelaciones) {

            if (!idsExistentes.contains(relacion.getId())) {
                usuario.agregarInscripcion(
                        new Inscripcion(usuario, relacion)
                );
            }
        }

        usuarioRepository.saveAndFlush(usuario);

        return ResponseEntity.ok(construirPerfil(usuario));
    }

    // =====================================
    // BUSCAR USUARIO AUTENTICADO
    // =====================================

    private Usuario buscarUsuario(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                "anonymousUser".equals(
                        authentication.getPrincipal()
                )) {

            return null;
        }

        String email = authentication.getName();

        return usuarioRepository
                .findByEmail(email)
                .orElse(null);
    }

    // =====================================
    // CONSTRUIR RESPUESTA DEL PERFIL
    // =====================================

    private PerfilUsuarioResponse construirPerfil(Usuario usuario) {

        List<PerfilInscripcionResponse> inscripciones =
                new ArrayList<>();

        for (Inscripcion inscripcion :
                usuario.getInscripciones()) {

            AnioCarrera relacion =
                    inscripcion.getAnioCarrera();

            inscripciones.add(
                    new PerfilInscripcionResponse(
                            relacion.getId(),
                            relacion.getCarrera().getId(),
                            relacion.getCarrera().getNombre(),
                            relacion.getAnio().getNumero()
                    )
            );
        }

        return new PerfilUsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().name(),
                inscripciones
        );
    }
}
