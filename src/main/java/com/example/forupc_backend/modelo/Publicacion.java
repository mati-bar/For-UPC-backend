package com.example.forupc_backend.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicacion")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @OneToMany(
            mappedBy = "publicacion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PublicacionDestino> destinos = new ArrayList<>();

    public Publicacion() {
    }

    public Publicacion(
            String titulo,
            String contenido,
            LocalDateTime fechaPublicacion
    ) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaPublicacion = fechaPublicacion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<PublicacionDestino> getDestinos() {
        return destinos;
    }

    public void setDestinos(List<PublicacionDestino> destinos) {
        this.destinos = destinos;
    }

    public void agregarDestino(PublicacionDestino destino) {
        destinos.add(destino);
        destino.setPublicacion(this);
    }

    public void eliminarDestino(PublicacionDestino destino) {
        destinos.remove(destino);
        destino.setPublicacion(null);
    }
}