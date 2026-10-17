package com.phantom.api.mapper;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.UsuarioResumenDTO;
import com.phantom.api.entity.Perfil;
import com.phantom.api.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(7L).nombre("Ana").email("ana@x.com").password("HASH").rol("USER")
                .dni("12345678").direccion("Av. 1").numeroTelefono("+51987654321")
                .fechaRegistro(Instant.parse("2026-01-01T05:00:00Z")).build();
    }

    private static Set<String> campos(Class<?> tipo) {
        return Arrays.stream(tipo.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("toDTO: no existe ningún campo de password en el DTO público del usuario")
    void dtoNoTienePassword() {
        assertFalse(campos(UsuarioDTO.class).contains("password"));
    }

    @Test
    @DisplayName("toDTO: la fecha de registro sale en hora de Perú (-05:00) y el perfil se incluye si existe")
    void toDTO() {
        usuario.setPerfil(Perfil.builder().id(3L).biografia("hola").usuario(usuario).build());

        UsuarioDTO dto = UsuarioMapper.toDTO(usuario);

        assertEquals("2026-01-01T00:00-05:00", dto.getFechaRegistro().toString());
        assertEquals(3L, dto.getPerfil().getId());
        assertEquals("12345678", dto.getDni());
    }

    @Test
    @DisplayName("toResumen: expone SOLO id, nombre y email (sin DNI, dirección, teléfono, rol ni password)")
    void resumenMinimo() {
        UsuarioResumenDTO resumen = UsuarioMapper.toResumen(usuario);

        assertEquals(Set.of("id", "nombre", "email"), campos(UsuarioResumenDTO.class));
        assertEquals(7L, resumen.getId());
        assertEquals("Ana", resumen.getNombre());
        assertEquals("ana@x.com", resumen.getEmail());
    }
}
