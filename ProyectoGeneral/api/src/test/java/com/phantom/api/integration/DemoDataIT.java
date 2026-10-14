package com.phantom.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Arranca la aplicación con los datos demo y la precarga de caché ACTIVADOS (como con el
 * perfil "demo") contra PostgreSQL real. Usa su propio contexto y contenedor.
 */
@TestPropertySource(properties = {"app.seed.enabled=true", "app.cache.warmup.enabled=true"})
class DemoDataIT extends AbstractIntegrationIT {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("se cargaron categorías, etiquetas, productos, usuarios, perfiles y ventas demo")
    void datosCargados() {
        assertEquals(5, jdbc.queryForObject("select count(*) from categorias", Integer.class));
        assertEquals(6, jdbc.queryForObject("select count(*) from etiquetas", Integer.class));
        assertTrue(jdbc.queryForObject("select count(*) from productos", Integer.class) >= 15);
        assertEquals(3, jdbc.queryForObject(
                "select count(*) from usuarios where email like '%@demo.local'", Integer.class));
        assertEquals(2, jdbc.queryForObject("select count(*) from perfiles", Integer.class));
        assertEquals(5, jdbc.queryForObject("select count(*) from ventas", Integer.class));
    }

    @Test
    @DisplayName("la BD aceptó los datos demo: stock >= 0 y totales de venta = suma de sus detalles")
    void integridad() {
        assertEquals(0, jdbc.queryForObject("select count(*) from productos where stock < 0", Integer.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from ventas v where v.total <> "
                        + "(select coalesce(sum(subtotal), 0) from detalle_ventas d where d.venta_id = v.id)",
                Integer.class));
        assertTrue(jdbc.queryForObject("select count(*) from productos where stock = 0", Integer.class) >= 1);
    }

    @Test
    @DisplayName("la caché ya está precargada al arrancar (listados y productos por ID)")
    void cachePrecargada() {
        assertNotNull(cacheManager.getCache("categorias").get("todas"));
        assertNotNull(cacheManager.getCache("etiquetas").get("todas"));
        assertNotNull(cacheManager.getCache("productos").get("todas"));
        Long idProducto = jdbc.queryForObject("select min(id) from productos", Long.class);
        assertNotNull(cacheManager.getCache("productos").get("id:" + idProducto));
    }

    @Test
    @DisplayName("el catálogo demo se sirve públicamente desde la API")
    void catalogoPublico() throws Exception {
        get("/api/v1/productos", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(15))));
        get("/api/v1/categorias", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("un usuario demo puede iniciar sesión (Demo12345) y ve SUS ventas con fecha en hora de Perú")
    void usuarioDemoYZonaHoraria() throws Exception {
        String token = json(post("/api/v1/auth/login", null,
                Map.of("email", "maria.quispe@demo.local", "password", "Demo12345"))
                .andExpect(status().isOk())).get("accessToken").asText();

        get("/api/v1/ventas/mias", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fecha", endsWith("-05:00")));
        get("/api/v1/auth/me", token)
                .andExpect(jsonPath("$.fechaRegistro", endsWith("-05:00")));
    }
}
