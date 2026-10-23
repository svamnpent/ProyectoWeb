package com.phantom.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    @DisplayName("los tres roles del sistema")
    void roles() {
        assertEquals(Set.of("USER", "TRABAJADOR", "ADMIN"), Roles.VALIDOS);
    }

    @Test
    @DisplayName("la expresión de validación acepta los roles (sin distinguir mayúsculas) y rechaza el resto")
    void regex() {
        Pattern p = Pattern.compile(Roles.REGEX);

        assertTrue(p.matcher("USER").matches());
        assertTrue(p.matcher("trabajador").matches());
        assertTrue(p.matcher("Admin").matches());
        assertFalse(p.matcher("ROOT").matches());
        assertFalse(p.matcher("ADMIN ").matches());
        assertFalse(p.matcher("USER|ADMIN").matches());
        assertFalse(p.matcher("").matches());
    }

    @Test
    @DisplayName("las expresiones de @PreAuthorize y el prefijo de autoridad son coherentes")
    void expresiones() {
        assertEquals("hasRole('ADMIN')", Roles.HAS_ADMIN);
        assertEquals("hasAnyRole('ADMIN','TRABAJADOR')", Roles.HAS_STAFF);
        assertEquals("ROLE_", Roles.AUTHORITY_PREFIX);
    }
}
