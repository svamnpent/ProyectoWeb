package com.phantom.api.security;

import java.util.Set;

/**
 * Única fuente de verdad de los roles. Antes los textos "USER", "ADMIN" y "TRABAJADOR" estaban
 * repetidos en servicios, DTOs, seguridad y seeders.
 *
 *  USER        comprador de la tienda
 *  TRABAJADOR  personal con acceso limitado al back-office
 *  ADMIN       administrador (acceso total)
 *
 * En la BD el rol se guarda como texto y la restricción chk_usuarios_rol (V4) admite estos tres
 * valores. En el JWT viaja en el claim "roles" y Spring lo convierte en la autoridad ROLE_<rol>.
 */
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
