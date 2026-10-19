
package com.example.forupc_backend.controller;

import com.example.forupc_backend.dto.*;
import com.example.forupc_backend.modelo.AnioCarrera;
import com.example.forupc_backend.modelo.Inscripcion;
import com.example.forupc_backend.modelo.Rol;
import com.example.forupc_backend.modelo.Usuario;
import com.example.forupc_backend.repository.AnioCarreraRepository;
import com.example.forupc_backend.repository.UsuarioRepository;
import com.example.forupc_backend.seguridad.JwtService;
import com.example.forupc_backend.service.BrevoEmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String MENSAJE_RECUPERACION =
            "Si el correo está registrado, recibirás instrucciones "
                    + "para recuperar tu contraseña.";

    private final UsuarioRepository usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AnioCarreraRepository anioCarreraRepository;
    private final BrevoEmailService brevoEmailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset-url}")
    private String passwordResetUrl;

    public AuthController(
            UsuarioRepository usuarioRepositorio,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AnioCarreraRepository anioCarreraRepository,
            BrevoEmailService brevoEmailService
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.anioCarreraRepository = anioCarreraRepository;
        this.brevoEmailService = brevoEmailService;
    }

    // =====================================
    // REGISTRO DE ESTUDIANTES
    // =====================================

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(
            @RequestBody RegistroRequest datos
    ) {

        if (datos == null ||
                datos.getEmail() == null ||
                datos.getEmail().isBlank() ||
                datos.getNombre() == null ||
                datos.getNombre().isBlank() ||
                datos.getApellido() == null ||
                datos.getApellido().isBlank() ||
                datos.getPassword() == null ||
                datos.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Todos los datos personales son obligatorios");
        }

        // Verificar si el correo ya existe
        if (usuarioRepositorio.findByEmail(datos.getEmail())
                .isPresent()) {

            return ResponseEntity.status(409)
                    .body("Ya existe un usuario con ese email");
        }

        // Verificar inscripciones
        if (datos.getInscripciones() == null ||
                datos.getInscripciones().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("El usuario debe tener al menos una inscripción");
        }

        Set<Integer> carrerasSeleccionadas = new HashSet<>();

        // Crear usuario estudiante
        Usuario nuevo = new Usuario(
                datos.getEmail(),
                datos.getNombre(),
                datos.getApellido(),
                passwordEncoder.encode(datos.getPassword()),
                Rol.ESTUDIANTE
        );

        // Recorrer las inscripciones
        for (InscripcionRequest solicitud :
                datos.getInscripciones()) {

            if (solicitud == null ||
                    solicitud.getAnioCarreraId() == null) {

                return ResponseEntity.badRequest()
                        .body(
                                "Cada inscripción debe indicar una carrera y un año"
                        );
            }

            AnioCarrera relacion = anioCarreraRepository
                    .findById(solicitud.getAnioCarreraId())
                    .orElse(null);

            if (relacion == null) {

                return ResponseEntity.badRequest()
                        .body(
                                "La combinación de carrera y año indicada no existe"
                        );
            }

            // Impedir la misma carrera con varios años
            Integer carreraId = relacion.getCarrera().getId();

            if (!carrerasSeleccionadas.add(carreraId)) {

                return ResponseEntity.badRequest()
                        .body(
                                "No podés seleccionar más de un año para la misma carrera"
                        );
            }

            nuevo.agregarInscripcion(
                    new Inscripcion(nuevo, relacion)
            );
        }

        // Guardar en Supabase
        usuarioRepositorio.save(nuevo);

        return ResponseEntity.status(201).build();
    }

    // =====================================
    // LOGIN JWT
    // =====================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest datos
    ) {

        Usuario usuario = usuarioRepositorio
                .findByEmail(datos.getEmail())
                .orElse(null);

        if (usuario == null ||
                datos.getPassword() == null ||
                !passwordEncoder.matches(
                        datos.getPassword(),
                        usuario.getPasswordHash()
                )) {

            return ResponseEntity.status(401)
                    .body("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(
                usuario.getEmail(),
                usuario.getRol().name()
        );

        return ResponseEntity.ok(new TokenResponse(token));
    }

    // =====================================
    // SOLICITAR RECUPERACION DE CONTRASEÑA
    // =====================================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> solicitarRecuperacion(
            @RequestBody ForgotPasswordRequest datos
    ) {

        if (datos == null ||
                datos.getEmail() == null ||
                datos.getEmail().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El correo electrónico es obligatorio.");
        }

        Usuario usuario = usuarioRepositorio
                .findByEmail(datos.getEmail())
                .orElse(null);

        // No revelar si el correo está registrado
        if (usuario == null) {
            return ResponseEntity.ok(MENSAJE_RECUPERACION);
        }

        // Generar token aleatorio de 256 bits
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        // Guardar el hash, nunca el token original
        usuario.setResetToken(calcularHash(token));

        // Vencimiento: 30 minutos
        usuario.setResetTokenExpiry(
                LocalDateTime.now().plusMinutes(30)
        );

        usuarioRepositorio.save(usuario);

        // Crear enlace de recuperación
        String enlace = passwordResetUrl + "?token=" + token;

        // Enviar correo mediante Brevo
        try {

            brevoEmailService.enviarRecuperacion(
                    usuario.getEmail(),
                    enlace
            );

            System.out.println(
                    "Brevo: solicitud de envío de recuperación aceptada."
            );

        } catch (Exception e) {

            // Diagnóstico sin revelar información sensible
            System.err.println(
                    "Error al enviar correo con Brevo: "
                            + e.getClass().getSimpleName()
            );
        }

        // Misma respuesta independientemente del resultado
        return ResponseEntity.ok(MENSAJE_RECUPERACION);
    }

    // =====================================
    // RESTABLECER CONTRASEÑA
    // =====================================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetearPassword(
            @RequestBody ResetPasswordRequest datos
    ) {

        if (datos == null ||
                datos.getToken() == null ||
                datos.getToken().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El token de recuperación es obligatorio.");
        }

        if (datos.getNewPassword() == null ||
                datos.getNewPassword().length() < 8) {

            return ResponseEntity.badRequest()
                    .body(
                            "La nueva contraseña debe tener al menos 8 caracteres."
                    );
        }

        String hashToken = calcularHash(datos.getToken());

        Usuario usuario = usuarioRepositorio
                .findByResetToken(hashToken)
                .orElse(null);

        // Validar token y vencimiento
        if (usuario == null ||
                usuario.getResetTokenExpiry() == null ||
                !usuario.getResetTokenExpiry()
                        .isAfter(LocalDateTime.now())) {

            return ResponseEntity.badRequest()
                    .body("El enlace no es válido o ya venció.");
        }

        // Actualizar contraseña con BCrypt
        usuario.setPasswordHash(
                passwordEncoder.encode(
                        datos.getNewPassword()
                )
        );

        // Invalidar token de recuperación
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);

        usuarioRepositorio.save(usuario);

        return ResponseEntity.ok(
                "Contraseña actualizada correctamente"
        );
    }

    // =====================================
    // CALCULAR HASH SHA-256
    // =====================================

    private String calcularHash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] resultado = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(resultado);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "No se pudo calcular el hash del token",
                    e
            );
        }
    }
}
