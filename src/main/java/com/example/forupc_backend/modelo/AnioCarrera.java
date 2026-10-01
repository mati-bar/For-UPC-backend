package com.example.forupc_backend.modelo;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class AnioCarrera {

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne
    @JoinColumn(name = "anio_id", nullable = false)
    private Anio anio;


}
