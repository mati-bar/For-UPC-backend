package com.example.forupc_backend.dto;

import java.util.List;

public record CarreraConAniosResponse(
        Integer id,
        String nombre,
        List<AnioCarreraResponse> anios
) {
}
