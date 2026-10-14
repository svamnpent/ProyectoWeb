package com.phantom.api.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String nombre, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(nombre, null, authorities));
    }

    @Test
    @DisplayName("getId / isAdmin / isSelf para un usuario normal")
    void usuarioNormal() {
        autenticarComo("5", "ROLE_USER");

        assertEquals(5L, currentUser.getId());
        assertFalse(currentUser.isAdmin());
        assertTrue(currentUser.isSelf(5L));
        assertFalse(currentUser.isSelf(6L));
        assertFalse(currentUser.isSelf(null));
    }

    @Test
    @DisplayName("isAdmin con ROLE_ADMIN")
    void administrador() {
        autenticarComo("1", "ROLE_ADMIN");

        assertTrue(currentUser.isAdmin());
    }

    @Test
    @DisplayName("requireSelfOrAdmin - dueño y ADMIN pasan; otro usuario es rechazado")
    void requireSelfOrAdmin() {
        autenticarComo("5", "ROLE_USER");
        assertDoesNotThrow(() -> currentUser.requireSelfOrAdmin(5L));
        assertThrows(AccessDeniedException.class, () -> currentUser.requireSelfOrAdmin(6L));
        assertThrows(AccessDeniedException.class, () -> currentUser.requireSelfOrAdmin(null));

        autenticarComo("1", "ROLE_ADMIN");
        assertDoesNotThrow(() -> currentUser.requireSelfOrAdmin(6L));
    }

    @Test
    @DisplayName("sin autenticación: findId vacío y getId lanza excepción")
    void sinAutenticacion() {
        assertTrue(currentUser.findId().isEmpty());
        assertFalse(currentUser.isAdmin());
        assertThrows(AuthenticationCredentialsNotFoundException.class, currentUser::getId);
        assertThrows(AccessDeniedException.class, () -> currentUser.requireSelfOrAdmin(5L));
    }

    @Test
    @DisplayName("usuario anónimo no se considera autenticado")
    void anonimo() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "clave", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertTrue(currentUser.findId().isEmpty());
    }

    @Test
    @DisplayName("un nombre no numérico no es un ID de usuario válido")
    void nombreNoNumerico() {
        autenticarComo("no-es-un-id", "ROLE_USER");

        assertTrue(currentUser.findId().isEmpty());
    }
}
