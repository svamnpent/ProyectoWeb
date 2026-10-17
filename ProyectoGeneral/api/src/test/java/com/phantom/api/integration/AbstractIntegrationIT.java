package com.phantom.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de los tests de integración: aplicación completa + PostgreSQL real + seguridad real
 * (JWT firmado de verdad). Cada test usa datos con nombres únicos, por lo que no dependen
 * del orden ni se estorban entre sí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgresTestcontainersConfig.class)
abstract class AbstractIntegrationIT {

    protected static final String ADMIN_EMAIL = "admin@test.local";   // creado por AdminBootstrap
    protected static final String ADMIN_PASSWORD = "Admin12345";
    protected static final String PASSWORD = "Secreta12345";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JdbcTemplate jdbc;

    protected String unico(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected JsonNode json(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    protected ResultActions post(String url, String token, Object body) throws Exception {
        var req = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (token != null) {
            req.header("Authorization", bearer(token));
        }
        return mockMvc.perform(req);
    }

    protected ResultActions put(String url, String token, Object body) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions get(String url, String token) throws Exception {
        var req = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url);
        if (token != null) {
            req.header("Authorization", bearer(token));
        }
        return mockMvc.perform(req);
    }

    protected ResultActions delete(String url, String token) throws Exception {
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url)
                .header("Authorization", bearer(token)));
    }

    // ---------------------------------------------------------------- fixtures

    protected String tokenAdmin() throws Exception {
        ResultActions r = post("/api/v1/auth/login", null,
                Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD)).andExpect(status().isOk());
        return json(r).get("accessToken").asText();
    }

    /** Registra un usuario nuevo y devuelve {token, id}. */
    protected Sesion registrarUsuario() throws Exception {
        String email = unico("user") + "@test.local";
        ResultActions r = post("/api/v1/auth/register", null,
                Map.of("nombre", "Usuario Test", "email", email, "password", PASSWORD))
                .andExpect(status().isCreated());
        String token = json(r).get("accessToken").asText();
        // La respuesta de register/login solo trae el token; el ID se obtiene de /auth/me
        long id = json(get("/api/v1/auth/me", token).andExpect(status().isOk())).get("id").asLong();
        return new Sesion(token, id, email);
    }

    /** El ADMIN da de alta a un trabajador (POST /backoffice/usuarios con rol TRABAJADOR) y este inicia sesión. */
    protected Sesion crearTrabajador() throws Exception {
        String email = unico("trabajador") + "@test.local";
        String admin = tokenAdmin();
        long id = json(post("/api/v1/backoffice/usuarios", admin, Map.of(
                "nombre", "Trabajador Test", "email", email, "password", PASSWORD, "rol", "TRABAJADOR"))
                .andExpect(status().isCreated())).get("id").asLong();
        String token = json(post("/api/v1/auth/login", null, Map.of("email", email, "password", PASSWORD))
                .andExpect(status().isOk())).get("accessToken").asText();
        return new Sesion(token, id, email);
    }

    protected long crearCategoria(String tokenAdmin) throws Exception {
        ResultActions r = post("/api/v1/backoffice/categorias", tokenAdmin, Map.of("nombre", unico("cat")))
                .andExpect(status().isCreated());
        return json(r).get("id").asLong();
    }

    protected long crearProducto(String tokenAdmin, long categoriaId, int stock, String precio) throws Exception {
        ResultActions r = post("/api/v1/backoffice/productos", tokenAdmin, Map.of(
                "nombre", unico("prod"), "precio", new java.math.BigDecimal(precio),
                "stock", stock, "categoriaId", categoriaId)).andExpect(status().isCreated());
        return json(r).get("id").asLong();
    }

    protected int stockEnBD(long productoId) {
        return jdbc.queryForObject("select stock from productos where id = ?", Integer.class, productoId);
    }

    protected Map<String, Object> venta(long productoId, int cantidad) {
        return Map.of("detalles", java.util.List.of(Map.of("productoId", productoId, "cantidad", cantidad)));
    }

    protected record Sesion(String token, long id, String email) {
    }
}
