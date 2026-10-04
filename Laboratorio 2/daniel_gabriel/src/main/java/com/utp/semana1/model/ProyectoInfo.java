package com.utp.semana1.model;

public class ProyectoInfo {
    private String curso;
    private String semana;
    private String tecnologia;
    private String estado;

    public ProyectoInfo(String curso, String semana, String tecnologia, String estado) {
        this.curso = curso;
        this.semana = semana;
        this.tecnologia = tecnologia;
        this.estado = estado;
    }

    public String getCurso() { return curso; }
    public String getSemana() { return semana; }
    public String getTecnologia() { return tecnologia; }
    public String getEstado() { return estado; }
}