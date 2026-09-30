package com.example.forupc_backend.dto;

public class InscripcionRequest {

    private Integer carreraId;
    private Integer anioId;

    public InscripcionRequest() {
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
}