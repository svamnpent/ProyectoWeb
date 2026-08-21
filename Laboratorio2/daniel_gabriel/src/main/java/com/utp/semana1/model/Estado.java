package com.utp.semana1.model;

public class Estado {
    private String aplicacion;
    private Boolean activo;
    private String mensaje;

    public Estado(String aplicacion, Boolean activo, String mensaje) {
        this.aplicacion = aplicacion;
        this.activo = activo;
        this.mensaje = mensaje;
    }

    public String getAplicacion() {
        return aplicacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
