package com.example.forupc_backend.dto;

import java.util.List;

/** Datos editables por un administrador. No admite cambios de contraseña ni rol. */
public record EditarAlumnoAdminRequest(
        String nombre,
        String apellido,
        String email,
        List<Integer> anioCarreraIds
) {}
