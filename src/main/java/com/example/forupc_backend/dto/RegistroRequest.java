package com.example.forupc_backend.dto;

import java.util.List;

public class RegistroRequest {

    private String email;
    private String nombre;
    private String apellido;
    private String password;
    private List<InscripcionRequest> inscripciones;

    public RegistroRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<InscripcionRequest> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<InscripcionRequest> inscripciones) {
        this.inscripciones = inscripciones;
    }
}