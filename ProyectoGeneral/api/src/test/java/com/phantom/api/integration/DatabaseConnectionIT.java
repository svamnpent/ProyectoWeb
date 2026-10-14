package com.phantom.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la conexión REAL con PostgreSQL: que la aplicación arranca contra una BD real,
 * que Flyway aplica TODAS las migraciones y que Hibernate (ddl-auto=validate) confirma que
 * las entidades coinciden con el esquema (si no coincidieran, el contexto ni siquiera cargaría).
 */
class DatabaseConnectionIT extends AbstractIntegrationIT {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("el pool entrega conexiones válidas a PostgreSQL")
    void conexionValida() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            assertTrue(c.isValid(2));
            assertEquals("PostgreSQL", c.getMetaData().getDatabaseProductName());
        }
        assertEquals(1, jdbc.queryForObject("select 1", Integer.class));
    }

    @Test
    @DisplayName("Flyway aplicó V1 y V2 sin errores")
    void migracionesAplicadas() {
        List<String> versiones = jdbc.queryForList(
                "select version from flyway_schema_history where success = true and version is not null "
                        + "order by installed_rank", String.class);

        assertTrue(versiones.contains("1"), "falta V1: " + versiones);
        assertTrue(versiones.contains("2"), "falta V2: " + versiones);
    }

    @Test
    @DisplayName("existen todas las tablas del esquema")
    void tablasExisten() {
        for (String tabla : List.of("categorias", "usuarios", "perfiles", "productos", "etiquetas",
                "producto_etiqueta", "ventas", "detalle_ventas")) {
            Integer n = jdbc.queryForObject(
                    "select count(*) from information_schema.tables where table_schema = 'public' and table_name = ?",
                    Integer.class, tabla);
            assertEquals(1, n, "falta la tabla " + tabla);
        }
    }

    @Test
    @DisplayName("V2: la BD rechaza stock negativo (última defensa contra sobreventa)")
    void restriccionStock() {
        jdbc.update("insert into categorias (nombre) values (?)", unico("cat"));
        Long categoriaId = jdbc.queryForObject("select max(id) from categorias", Long.class);

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "insert into productos (nombre, precio, stock, categoria_id) values (?, 10, -1, ?)",
                unico("prod"), categoriaId));
    }

    @Test
    @DisplayName("V2: la BD rechaza roles inválidos")
    void restriccionRol() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "insert into usuarios (nombre, email, rol) values ('x', ?, 'HACKER')",
                unico("mail") + "@x.com"));
    }

    @Test
    @DisplayName("V2: existen los índices sobre claves foráneas")
    void indicesCreados() {
        List<String> indices = jdbc.queryForList(
                "select indexname from pg_indexes where schemaname = 'public'", String.class);

        assertTrue(indices.containsAll(List.of("idx_productos_categoria", "idx_ventas_usuario_fecha",
                "idx_detalle_venta", "idx_detalle_producto", "idx_pe_etiqueta")), indices.toString());
    }

    @Test
    @DisplayName("el administrador inicial se creó con password hasheado con BCrypt")
    void adminBootstrap() {
        String hash = jdbc.queryForObject("select password from usuarios where email = ?",
                String.class, ADMIN_EMAIL);
        String rol = jdbc.queryForObject("select rol from usuarios where email = ?", String.class, ADMIN_EMAIL);

        assertTrue(hash.startsWith("$2"), "no parece BCrypt: " + hash);
        assertEquals("ADMIN", rol);
    }

    @Test
    @DisplayName("/actuator/health es público y reporta UP (incluye la BD)")
    void health() throws Exception {
        get("/actuator/health", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
