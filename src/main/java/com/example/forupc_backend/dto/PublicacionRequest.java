package com.example.forupc_backend.dto;

import java.util.List;
import com.example.forupc_backend.modelo.DuracionPublicacion;

public class PublicacionRequest {

    private String titulo;
    private String mensaje;
    private DuracionPublicacion duracion;
    private boolean urgente;
    private List<DestinoRequest> inscripciones;

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

    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

    public List<DestinoRequest> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<DestinoRequest> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public static class DestinoRequest {

        private Integer anioCarreraId;

        public DestinoRequest() {
        }

        public Integer getAnioCarreraId() {
            return anioCarreraId;
        }

        public void setAnioCarreraId(Integer anioCarreraId) {
            this.anioCarreraId = anioCarreraId;
        }
    }
}
