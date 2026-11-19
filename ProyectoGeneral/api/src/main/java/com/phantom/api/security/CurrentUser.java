package com.phantom.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Identidad y permisos del usuario de la petición. */
@Component("currentUser")
public class CurrentUser {

    private static final String ROLE_ADMIN = Roles.AUTHORITY_PREFIX + Roles.ADMIN;
    private static final String ROLE_TRABAJADOR = Roles.AUTHORITY_PREFIX + Roles.TRABAJADOR;

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
        return tieneAutoridad(ROLE_ADMIN);
    }

    /** Personal de la tienda: ADMIN o TRABAJADOR (usuarios del back-office). */
    public boolean isStaff() {
        return isAdmin() || tieneAutoridad(ROLE_TRABAJADOR);
    }

    private boolean tieneAutoridad(String autoridad) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> autoridad.equals(a.getAuthority()));
    }

    public boolean isSelf(Long id) {
        return id != null && findId().filter(id::equals).isPresent();
    }

    /** Autoriza al titular de la venta o al personal. */
    public void requireSelfOrStaff(Long ownerId) {
        if (isStaff()) {
            return;
        }
        if (!isSelf(ownerId)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este recurso");
        }
    }

    /** Autoriza al titular de los datos personales o a un administrador. */
    public void requireSelfOrAdmin(Long ownerId) {
        if (isAdmin()) {
            return;
        }
        if (!isSelf(ownerId)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este recurso");
        }
    }
}
