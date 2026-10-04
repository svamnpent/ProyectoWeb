package com.utp.semana2.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta) {
}