package com.phantom.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Limita los intentos fallidos por correo en esta instancia. */
@Component
public class LoginAttemptService {

    private final int maxAttempts;
    private final Cache<String, Integer> intentos;

    @Autowired
    public LoginAttemptService(@Value("${app.security.login.max-attempts:5}") int maxAttempts,
                               @Value("${app.security.login.lock-minutes:15}") long lockMinutes) {
        this(maxAttempts, Duration.ofMinutes(lockMinutes), Ticker.systemTicker());
    }

    LoginAttemptService(int maxAttempts, Duration lockDuration, Ticker ticker) {
        this.maxAttempts = maxAttempts;
        this.intentos = Caffeine.newBuilder()
                .expireAfterWrite(lockDuration)
                .maximumSize(10_000)
                .ticker(ticker)
                .build();
    }

    public void registrarFallo(String email) {
        intentos.asMap().merge(clave(email), 1, Integer::sum);
    }

    public void registrarExito(String email) {
        intentos.invalidate(clave(email));
    }

    public boolean estaBloqueado(String email) {
        Integer fallos = intentos.getIfPresent(clave(email));
        return fallos != null && fallos >= maxAttempts;
    }

    private String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
