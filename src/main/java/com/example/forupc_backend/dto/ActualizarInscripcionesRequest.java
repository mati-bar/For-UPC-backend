package com.example.forupc_backend.dto;

import java.util.List;

public record ActualizarInscripcionesRequest(
        List<Integer> anioCarreraIds
) {
}
