package com.example.forupc_backend.controller;

import com.example.forupc_backend.dto.*;
import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Inscripcion;
import com.example.forupc_backend.modelo.Rol;
import com.example.forupc_backend.modelo.Usuario;
import com.example.forupc_backend.repository.AnioCarreraRepository;
import com.example.forupc_backend.repository.UsuarioRepository;
import com.example.forupc_backend.seguridad.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AnioCarreraRepository anioCarreraRepository;

    public AuthController(
            UsuarioRepository usuarioRepositorio,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AnioCarreraRepository anioCarreraRepository
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.anioCarreraRepository = anioCarreraRepository;
    }

    // =========================
    // REGISTRO
    // =========================

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest datos) {

        // Verificar si ya existe el email
        if (usuarioRepositorio.findByEmail(datos.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(409)
                    .body("Ya existe un usuario con ese email");
        }

        // Verificar que tenga al menos una inscripción
        if (datos.getInscripciones() == null ||
                datos.getInscripciones().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("El usuario debe tener al menos una inscripción");
        }

        // Crear usuario
        Usuario nuevo = new Usuario(
                datos.getEmail(),
                datos.getNombre(),
                datos.getApellido(),
                passwordEncoder.encode(datos.getPassword()),
                Rol.ESTUDIANTE
        );

        // Crear las inscripciones
        for (InscripcionRequest inscripcionRequest : datos.getInscripciones()) {

            // Buscar la combinación carrera + año
            AnioCarrera anioCarrera = anioCarreraRepository
                    .findById(inscripcionRequest.getAnioCarreraId())
                    .orElse(null);

            if (anioCarrera == null) {
                return ResponseEntity
                        .badRequest()
                        .body("La combinación de carrera y año indicada no existe");
            }

            // Crear inscripción
            Inscripcion inscripcion = new Inscripcion(
                    nuevo,
                    anioCarrera
            );

            // Agregar inscripción al usuario
            nuevo.agregarInscripcion(inscripcion);
        }

        // Guardar usuario y sus inscripciones
        usuarioRepositorio.save(nuevo);

        return ResponseEntity
                .status(201)
                .build();
    }


    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {

        Usuario usuario = usuarioRepositorio
                .findByEmail(datos.getEmail())
                .orElse(null);

        // Verificar usuario y contraseña
        if (usuario == null ||
                !passwordEncoder.matches(
                        datos.getPassword(),
                        usuario.getPasswordHash()
                )) {

            return ResponseEntity
                    .status(401)
                    .body("Email o contraseña incorrectos");
        }

        // Generar JWT
        String token = jwtService.generarToken(
                usuario.getEmail(),
                usuario.getRol().name()
        );

        return ResponseEntity.ok(
                new TokenResponse(token)
        );
    }


    // =========================
    // RESET PASSWORD
    // =========================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetearPassword(
            @RequestBody ResetPasswordRequest datos
    ) {

        Usuario usuario = usuarioRepositorio
                .findByResetToken(datos.getToken())
                .orElse(null);

        // Verificar token
        if (usuario == null ||
                usuario.getResetTokenExpiry() == null ||
                usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

            return ResponseEntity
                    .status(400)
                    .body("El link no es válido o ya venció");
        }

        // Cambiar contraseña
        usuario.setPasswordHash(
                passwordEncoder.encode(datos.getNewPassword())
        );

        // Eliminar token utilizado
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);

        // Guardar cambios
        usuarioRepositorio.save(usuario);

        return ResponseEntity.ok(
                "Contraseña actualizada correctamente"
        );
    }
}