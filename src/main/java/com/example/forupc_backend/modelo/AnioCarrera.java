package com.example.forupc_backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(
        name = "anio_carrera",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"carrera_id", "anio_id"}
                )
        }
)
public class AnioCarrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne
    @JoinColumn(name = "anio_id", nullable = false)
    private Anio anio;

    public AnioCarrera() {
    }

    public AnioCarrera(Carrera carrera, Anio anio) {
        this.carrera = carrera;
        this.anio = anio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public Anio getAnio() {
        return anio;
    }

    public void setAnio(Anio anio) {
        this.anio = anio;
    }
}
