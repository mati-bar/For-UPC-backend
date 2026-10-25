package com.example.forupc_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PublicacionesPaginadasResponse(
        List<PublicacionItem> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean first,
        boolean last,
        boolean empty
) {
    public record PublicacionItem(
            Integer id,
            String titulo,
            String mensaje,
            LocalDateTime fecha,
            LocalDateTime fechaExpiracion,
            boolean urgente,
            String imagenUrl,
            String archivoUrl,
            String archivoNombre,
            Integer carreraId,
            Integer anioId,
            List<DestinoItem> destinos
    ) {}

    public record DestinoItem(
            Integer id,
            CarreraItem carrera,
            AnioItem anio
    ) {}

    public record CarreraItem(Integer id, String nombre) {}

    public record AnioItem(Integer id, Integer numero) {}
}
