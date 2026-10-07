package com.example.forupc_backend.modelo;

public class PublicacionRequest {

    private String titulo;
    private String mensaje;

    private Integer carreraId;
    private Integer anioId;

    private DuracionPublicacion duracion;

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

    public DuracionPublicacion getDuracion() {
        return duracion;
    }

    public void setDuracion(DuracionPublicacion duracion) {
        this.duracion = duracion;
    }
}