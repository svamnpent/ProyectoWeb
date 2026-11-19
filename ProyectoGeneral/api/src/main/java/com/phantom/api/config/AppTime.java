package com.phantom.api.config;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Convierte los instantes a la hora de la tienda en Perú. */
public final class AppTime {

    public static final ZoneId ZONA = ZoneId.of("America/Lima");

    private AppTime() {
    }

    public static OffsetDateTime aLima(Instant instante) {
        return instante == null ? null : instante.atZone(ZONA).toOffsetDateTime();
    }
}
