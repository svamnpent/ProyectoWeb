package com.utp.semana4_api_rest.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int estado;
    private String mensaje;
    private String ruta;
    private LocalDateTime fechaHora;

    public ErrorResponse(int estado, String mensaje, String ruta) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.fechaHora = LocalDateTime.now();
    }

    public int getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}