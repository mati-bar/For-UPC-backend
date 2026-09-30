package com.example.forupc_backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inscripcion",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"usuario_id", "carrera_id"}
                )
        }
)
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne
    @JoinColumn(name = "anio_id", nullable = false)
    private Anio anio;

    public Inscripcion() {
    }

    public Inscripcion(
            Usuario usuario,
            Carrera carrera,
            Anio anio
    ) {
        this.usuario = usuario;
        this.carrera = carrera;
        this.anio = anio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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