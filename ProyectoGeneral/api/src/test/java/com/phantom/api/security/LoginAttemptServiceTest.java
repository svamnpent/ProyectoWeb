package com.phantom.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private final AtomicLong nanos = new AtomicLong();
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        // Reloj falso: permite avanzar el tiempo sin esperar
        service = new LoginAttemptService(3, Duration.ofMinutes(15), nanos::get);
    }

    private void avanzar(Duration duracion) {
        nanos.addAndGet(duracion.toNanos());
    }

    @Test
    @DisplayName("no bloquea antes de alcanzar el máximo de intentos")
    void noBloqueaAntesDelMaximo() {
        service.registrarFallo("ana@x.com");
        service.registrarFallo("ana@x.com");

        assertFalse(service.estaBloqueado("ana@x.com"));
    }

    @Test
    @DisplayName("bloquea al alcanzar el máximo de intentos fallidos")
    void bloqueaAlAlcanzarElMaximo() {
        for (int i = 0; i < 3; i++) {
            service.registrarFallo("ana@x.com");
        }

        assertTrue(service.estaBloqueado("ana@x.com"));
    }

    @Test
    @DisplayName("un login correcto reinicia el contador")
    void exitoReiniciaContador() {
        service.registrarFallo("ana@x.com");
        service.registrarFallo("ana@x.com");
        service.registrarExito("ana@x.com");
        service.registrarFallo("ana@x.com");

        assertFalse(service.estaBloqueado("ana@x.com"));
    }

    @Test
    @DisplayName("el bloqueo expira pasado el tiempo configurado")
    void bloqueoExpira() {
        for (int i = 0; i < 3; i++) {
            service.registrarFallo("ana@x.com");
        }
        avanzar(Duration.ofMinutes(14));
        assertTrue(service.estaBloqueado("ana@x.com"));

        avanzar(Duration.ofMinutes(2));
        assertFalse(service.estaBloqueado("ana@x.com"));
    }

    @Test
    @DisplayName("el email no distingue mayúsculas ni espacios, y cada email cuenta por separado")
    void claveNormalizada() {
        service.registrarFallo("ANA@x.com");
        service.registrarFallo(" ana@X.com ");
        service.registrarFallo("ana@x.com");

        assertTrue(service.estaBloqueado("Ana@X.com"));
        assertFalse(service.estaBloqueado("luis@x.com"));
    }
}
