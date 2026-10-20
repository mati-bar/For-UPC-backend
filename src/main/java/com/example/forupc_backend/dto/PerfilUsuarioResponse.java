
package com.example.forupc_backend.dto;

import java.util.List;

public record PerfilUsuarioResponse(
        Integer id,
        String nombre,
        String apellido,
        String email,
        String rol,
        List<PerfilInscripcionResponse> inscripciones
) {
}
