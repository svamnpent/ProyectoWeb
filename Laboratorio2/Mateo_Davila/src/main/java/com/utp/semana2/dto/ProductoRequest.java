package com.utp.semana2.dto;

public record ProductoRequest(
        String nombre,
        double precio,
        int stock) {
}