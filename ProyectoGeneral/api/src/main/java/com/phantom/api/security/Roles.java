package com.phantom.api.security;

import java.util.Set;

/** Roles compartidos por la API, la persistencia y los tokens. */
public final class Roles {

    public static final String USER = "USER";
    public static final String TRABAJADOR = "TRABAJADOR";
    public static final String ADMIN = "ADMIN";

    /** Prefijo que Spring Security antepone a los roles al construir las autoridades. */
    public static final String AUTHORITY_PREFIX = "ROLE_";

    public static final Set<String> VALIDOS = Set.of(USER, TRABAJADOR, ADMIN);

    /** Expresión de validación para DTOs (sin distinguir mayúsculas). */
    public static final String REGEX = "^(?i)(" + USER + "|" + TRABAJADOR + "|" + ADMIN + ")$";

    /** Expresiones para @PreAuthorize. */
    public static final String HAS_ADMIN = "hasRole('" + ADMIN + "')";
    public static final String HAS_STAFF = "hasAnyRole('" + ADMIN + "','" + TRABAJADOR + "')";

    private Roles() {
    }
}
