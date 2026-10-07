package com.example.forupc_backend.modelo;

public class PublicacionRequest {

    private String titulo;
    private String mensaje;
    private DuracionPublicacion duracion;
    private Integer carreraId;
    private Integer anioId;
    private boolean urgente;

    public PublicacionRequest() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public DuracionPublicacion getDuracion() {
        return duracion;
    }

    public void setDuracion(DuracionPublicacion duracion) {
        this.duracion = duracion;
    }

    public Integer getCarreraId() {
        return carreraId;
    }

    public void setCarreraId(Integer carreraId) {
        this.carreraId = carreraId;
    }

    public Integer getAnioId() {
        return anioId;
    }

    public void setAnioId(Integer anioId) {
        this.anioId = anioId;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }
}