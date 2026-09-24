package com.example.forupc_backend.controller;

import com.example.demo.dto.*;
import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.UsuarioRepository;
import com.example.demo.seguridad.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepositorio,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest datos) {
        if (usuarioRepositorio.findByEmail(datos.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("Ya existe un usuario con ese email");
        }

        Usuario nuevo = new Usuario(
                datos.getEmail(),
                datos.getNombre(),
                passwordEncoder.encode(datos.getPassword()),
                Rol.CLIENTE
        );
        usuarioRepositorio.save(nuevo);

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest datos) {
        Usuario usuario = usuarioRepositorio.findByEmail(datos.getEmail()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(datos.getPassword(), usuario.getPasswordHash())) {
            return ResponseEntity.status(401).body("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name());
        return ResponseEntity.ok(new TokenResponse(token));
    }

//    @PostMapping("/forgot-password")
//    public ResponseEntity<?> olvideMiPassword(@RequestBody ForgotPasswordRequest datos) {
//        Usuario usuario = usuarioRepositorio.findByEmail(datos.getEmail()).orElse(null);
//
//        // Responder siempre 200 exista o no el email, para no revelar qué emails están registrados
//        if (usuario != null) {
//            String token = UUID.randomUUID().toString();
//            usuario.setResetToken(token);
//            usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
//            usuarioRepositorio.save(usuario);
//
//            emailService.enviarEmailRecuperacion(usuario.getEmail(), token);
//        }
//
//        return ResponseEntity.ok("Si el email existe, vas a recibir un link para restablecer tu contraseña");
//    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetearPassword(@RequestBody ResetPasswordRequest datos) {
        Usuario usuario = usuarioRepositorio.findByResetToken(datos.getToken()).orElse(null);

        if (usuario == null || usuario.getResetTokenExpiry() == null
                || usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("El link no es válido o ya venció");
        }

        usuario.setPasswordHash(passwordEncoder.encode(datos.getNewPassword()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepositorio.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
