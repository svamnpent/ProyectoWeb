package com.phantom.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La caché local funcionando junto a la BD real. Truco de las pruebas: se modifica la fila
 * DIRECTAMENTE por JDBC (saltándose la API). Si la API sigue devolviendo el valor antiguo, la
 * respuesta salió de la caché; si tras una escritura por la API devuelve el nuevo, la invalidación
 * funciona.
 */
class CacheIT extends AbstractIntegrationIT {

    @Autowired
    private CacheManager cacheManager;

    private Map<String, Object> cuerpoProducto(String nombre, long categoriaId, int stock) {
        return Map.of("nombre", nombre, "precio", new BigDecimal("10.00"), "stock", stock,
                "categoriaId", categoriaId);
    }

    @Test
    @DisplayName("la segunda lectura pública de un producto sale de la caché, no de la BD")
    void segundaLecturaDesdeCache() throws Exception {
        String admin = tokenAdmin();
        long categoria = crearCategoria(admin);
        long id = crearProducto(admin, categoria, 5, "10.00");

        assertNull(cacheManager.getCache("productos").get("id:" + id), "aún no debería estar cacheado");
        String nombreOriginal = json(get("/api/v1/productos/" + id, null).andExpect(status().isOk()))
                .get("nombre").asText();
        assertNotNull(cacheManager.getCache("productos").get("id:" + id), "debería haberse cacheado");

        // Cambio "a escondidas" directamente en la BD
        jdbc.update("update productos set nombre = 'CAMBIADO-EN-BD' where id = ?", id);

        get("/api/v1/productos/" + id, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is(nombreOriginal))); // sigue sirviendo el valor cacheado
    }

    @Test
    @DisplayName("actualizar un producto por la API invalida la caché y se ve el dato nuevo")
    void actualizarInvalida() throws Exception {
        String admin = tokenAdmin();
        long categoria = crearCategoria(admin);
        long id = crearProducto(admin, categoria, 5, "10.00");
        get("/api/v1/productos/" + id, null).andExpect(status().isOk());

        put("/api/v1/backoffice/productos/" + id, admin, cuerpoProducto("Nombre-Nuevo", categoria, 5))
                .andExpect(status().isOk());

        get("/api/v1/productos/" + id, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Nombre-Nuevo")));
    }

    @Test
    @DisplayName("renombrar una categoría invalida los productos cacheados que la embeben")
    void categoriaInvalidaProductos() throws Exception {
        String admin = tokenAdmin();
        long categoria = crearCategoria(admin);
        long id = crearProducto(admin, categoria, 5, "10.00");
        get("/api/v1/productos/" + id, null).andExpect(status().isOk());
        String nuevoNombre = unico("renombrada");

        put("/api/v1/backoffice/categorias/" + categoria, admin, Map.of("nombre", nuevoNombre)).andExpect(status().isOk());

        get("/api/v1/productos/" + id, null)
                .andExpect(jsonPath("$.categoria.nombre", is(nuevoNombre)));
    }

    @Test
    @DisplayName("una compra invalida la caché: el stock mostrado es el real, no uno viejo")
    void compraActualizaStockMostrado() throws Exception {
        String admin = tokenAdmin();
        long id = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion ana = registrarUsuario();
        get("/api/v1/productos/" + id, null).andExpect(jsonPath("$.stock", is(5))); // queda cacheado

        post("/api/v1/ventas", ana.token(), venta(id, 2)).andExpect(status().isCreated());

        get("/api/v1/productos/" + id, null).andExpect(jsonPath("$.stock", is(3)));
    }

    @Test
    @DisplayName("una compra rechazada no deja la caché inconsistente")
    void compraRechazadaNoAlteraStockMostrado() throws Exception {
        String admin = tokenAdmin();
        long id = crearProducto(admin, crearCategoria(admin), 2, "10.00");
        Sesion ana = registrarUsuario();
        get("/api/v1/productos/" + id, null).andExpect(jsonPath("$.stock", is(2)));

        post("/api/v1/ventas", ana.token(), venta(id, 50)).andExpect(status().isBadRequest());

        get("/api/v1/productos/" + id, null).andExpect(jsonPath("$.stock", is(2)));
    }

    @Test
    @DisplayName("el listado de categorías se cachea y crear una categoría lo invalida")
    void categoriasListadoCacheYCrear() throws Exception {
        String admin = tokenAdmin();
        get("/api/v1/categorias", null).andExpect(status().isOk());
        assertNotNull(cacheManager.getCache("categorias").get("todas"));

        crearCategoria(admin);

        assertNull(cacheManager.getCache("categorias").get("todas"), "crear debe invalidar el listado");
    }
}
