package com.phantom.api.config;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Zona horaria de la tienda: Perú (America/Lima, UTC-5, sin horario de verano).
 *
 * En la BD las fechas son timestamptz (instantes absolutos); aquí solo se decide cómo se
 * MUESTRAN en la API: con el desfase -05:00 (p. ej. 2026-10-13T20:47:56-05:00).
 */
public final class AppTime {

    public static final ZoneId ZONA = ZoneId.of("America/Lima");

    private AppTime() {
    }

    public static OffsetDateTime aLima(Instant instante) {
        return instante == null ? null : instante.atZone(ZONA).toOffsetDateTime();
    }
}
