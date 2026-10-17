package com.phantom.api.support;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Autenticación JWT simulada para los tests de controllers (@WebMvcTest).
 * El "sub" es el ID del usuario, igual que en los tokens reales.
 */
public final class JwtTestUtils {

    private JwtTestUtils() {
    }

    public static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("1")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /** Personal de la tienda con acceso limitado al back-office. */
    public static RequestPostProcessor trabajador() {
        return jwt().jwt(j -> j.subject("2")).authorities(new SimpleGrantedAuthority("ROLE_TRABAJADOR"));
    }

    public static RequestPostProcessor user(long id) {
        return jwt().jwt(j -> j.subject(String.valueOf(id))).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
