package com.example.forupc_backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inscripcion",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"usuario_id", "anio_carrera_id"}
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
    @JoinColumn(name = "anio_carrera_id", nullable = false)
    private AnioCarrera anioCarrera;

    public Inscripcion() {
    }

    public Inscripcion(
            Usuario usuario,
            AnioCarrera anioCarrera
    ) {
        this.usuario = usuario;
        this.anioCarrera = anioCarrera;
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

    public AnioCarrera getAnioCarrera() {
        return anioCarrera;
    }

    public void setAnioCarrera(AnioCarrera anioCarrera) {
        this.anioCarrera = anioCarrera;
    }
}