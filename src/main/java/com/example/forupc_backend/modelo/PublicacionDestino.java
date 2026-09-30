package com.example.forupc_backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(
        name = "publicacion_destino",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "publicacion_id",
                                "carrera_id",
                                "anio_id"
                        }
                )
        }
)
public class PublicacionDestino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne
    @JoinColumn(name = "anio_id", nullable = false)
    private Anio anio;

    public PublicacionDestino() {
    }

    public PublicacionDestino(
            Publicacion publicacion,
            Carrera carrera,
            Anio anio
    ) {
        this.publicacion = publicacion;
        this.carrera = carrera;
        this.anio = anio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
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