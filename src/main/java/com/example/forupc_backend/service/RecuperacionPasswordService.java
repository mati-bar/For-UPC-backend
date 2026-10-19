
package com.example.forupc_backend.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RecuperacionPasswordService {

    private final SecureRandom secureRandom = new SecureRandom();

    // Genera un token aleatorio de 256 bits
    public String generarToken() {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Almacenaremos solamente el hash en PostgreSQL
    public String calcularHash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "No se pudo calcular el hash del token",
                    e
            );
        }
    }

    // Cada token vence a los 30 minutos
    public LocalDateTime calcularVencimiento() {
        return LocalDateTime.now().plusMinutes(30);
    }
}
