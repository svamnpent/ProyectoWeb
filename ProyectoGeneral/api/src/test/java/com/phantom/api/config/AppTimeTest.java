package com.phantom.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AppTimeTest {

    @Test
    @DisplayName("la zona de la tienda es America/Lima")
    void zona() {
        assertEquals("America/Lima", AppTime.ZONA.getId());
    }

    @Test
    @DisplayName("convierte un instante UTC a hora de Perú (UTC-5)")
    void convierteAHoraDePeru() {
        OffsetDateTime lima = AppTime.aLima(Instant.parse("2026-01-01T05:00:00Z"));

        assertEquals(ZoneOffset.ofHours(-5), lima.getOffset());
        assertEquals(2026, lima.getYear());
        assertEquals(0, lima.getHour());
        assertEquals(1, lima.getDayOfMonth());
    }

    @Test
    @DisplayName("el instante no cambia, solo su representación (medianoche de Lima del 1 ene = 05:00Z)")
    void mismoInstante() {
        Instant original = Instant.parse("2026-03-15T17:30:00Z");

        assertEquals(original, AppTime.aLima(original).toInstant());
    }

    @Test
    @DisplayName("Perú no tiene horario de verano: en cualquier mes el desfase es -05:00")
    void sinHorarioDeVerano() {
        assertEquals(ZoneOffset.ofHours(-5), AppTime.aLima(Instant.parse("2026-01-15T12:00:00Z")).getOffset());
        assertEquals(ZoneOffset.ofHours(-5), AppTime.aLima(Instant.parse("2026-07-15T12:00:00Z")).getOffset());
    }

    @Test
    @DisplayName("null se mantiene null")
    void nulo() {
        assertNull(AppTime.aLima(null));
    }
}
