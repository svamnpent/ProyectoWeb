package com.phantom.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Acceso al usuario autenticado de la petición actual (el "sub" del JWT es el ID del usuario).
 * También se usa desde expresiones SpEL: {@code @PreAuthorize("hasRole('ADMIN') or @currentUser.isSelf(#id)")}.
 */
@Component("currentUser")
public class CurrentUser {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    public Optional<Long> findId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(auth.getName()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Long getId() {
        return findId().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado"));
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    public boolean isSelf(Long id) {
        return id != null && findId().filter(id::equals).isPresent();
    }

    /**
     * Permite el acceso si el usuario actual es ADMIN o es el dueño del recurso.
     */
    public void requireSelfOrAdmin(Long ownerId) {
        if (isAdmin()) {
            return;
        }
        if (!isSelf(ownerId)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este recurso");
        }
    }
}
