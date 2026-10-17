package com.phantom.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reglas de negocio de ventas y stock contra PostgreSQL real: transacciones, rollback,
 * propiedad de los recursos y concurrencia.
 */
class VentaFlowIT extends AbstractIntegrationIT {

    @Test
    @DisplayName("comprar descuenta stock, calcula totales y asigna la venta al usuario autenticado")
    void compraDescuentaStock() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion ana = registrarUsuario();
        Sesion luis = registrarUsuario();

        // Ana intenta comprar "a nombre de Luis": debe ser ignorado
        Map<String, Object> cuerpo = Map.of("usuarioId", luis.id(),
                "detalles", List.of(Map.of("productoId", productoId, "cantidad", 2)));

        post("/api/v1/ventas", ana.token(), cuerpo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.id", is((int) ana.id())))
                .andExpect(jsonPath("$.total", is(20.00)))
                .andExpect(jsonPath("$.cantidadItems", is(2)))
                .andExpect(jsonPath("$.detalles", hasSize(1)));

        assertEquals(3, stockEnBD(productoId));
    }

    @Test
    @DisplayName("stock insuficiente -> 400 y el stock no cambia")
    void stockInsuficiente() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 3, "10.00");
        Sesion ana = registrarUsuario();

        post("/api/v1/ventas", ana.token(), venta(productoId, 10))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Stock Insuficiente")))
                .andExpect(jsonPath("$.message", containsString("solicitado 10, disponible 3")));

        assertEquals(3, stockEnBD(productoId));
        assertEquals(0, jdbc.queryForObject("select count(*) from ventas where usuario_id = ?",
                Integer.class, ana.id()));
    }

    @Test
    @DisplayName("ROLLBACK real: si un producto no tiene stock, no se descuenta el de los demás")
    void rollbackSiFallaUnProducto() throws Exception {
        String admin = tokenAdmin();
        long categoria = crearCategoria(admin);
        long conStock = crearProducto(admin, categoria, 5, "10.00");
        long casiAgotado = crearProducto(admin, categoria, 1, "10.00");
        Sesion ana = registrarUsuario();

        post("/api/v1/ventas", ana.token(), Map.of("detalles", List.of(
                Map.of("productoId", conStock, "cantidad", 2),
                Map.of("productoId", casiAgotado, "cantidad", 5))))
                .andExpect(status().isBadRequest());

        assertEquals(5, stockEnBD(conStock), "el descuento del primer producto debió revertirse");
        assertEquals(1, stockEnBD(casiAgotado));
    }

    @Test
    @DisplayName("producto inexistente -> 404")
    void productoInexistente() throws Exception {
        Sesion ana = registrarUsuario();

        post("/api/v1/ventas", ana.token(), venta(999_999_999L, 1)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("un usuario solo ve sus ventas; el ADMIN ve todas; el listado global es solo ADMIN")
    void propiedadDeVentas() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 10, "5.00");
        Sesion ana = registrarUsuario();
        Sesion luis = registrarUsuario();

        long ventaId = json(post("/api/v1/ventas", ana.token(), venta(productoId, 1))
                .andExpect(status().isCreated())).get("id").asLong();

        get("/api/v1/ventas/" + ventaId, ana.token()).andExpect(status().isOk());
        get("/api/v1/ventas/" + ventaId, admin).andExpect(status().isOk());
        get("/api/v1/ventas/" + ventaId, luis.token()).andExpect(status().isForbidden());
        get("/api/v1/ventas/" + ventaId + "/detalles", luis.token()).andExpect(status().isForbidden());
        get("/api/v1/ventas/" + ventaId + "/detalles", ana.token())
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        get("/api/v1/ventas/mias", ana.token()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        get("/api/v1/ventas/mias", luis.token()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        get("/api/v1/backoffice/ventas", luis.token()).andExpect(status().isForbidden());
        get("/api/v1/backoffice/ventas", admin).andExpect(status().isOk());
    }

    @Test
    @DisplayName("eliminar una venta (solo ADMIN) repone el stock")
    void eliminarVentaReponeStock() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion ana = registrarUsuario();
        long ventaId = json(post("/api/v1/ventas", ana.token(), venta(productoId, 4))
                .andExpect(status().isCreated())).get("id").asLong();
        assertEquals(1, stockEnBD(productoId));

        delete("/api/v1/backoffice/ventas/" + ventaId, ana.token()).andExpect(status().isForbidden());
        assertEquals(1, stockEnBD(productoId));

        delete("/api/v1/backoffice/ventas/" + ventaId, admin).andExpect(status().isNoContent());
        assertEquals(5, stockEnBD(productoId));
        assertEquals(0, jdbc.queryForObject("select count(*) from ventas where id = ?", Integer.class, ventaId));
    }

    @Test
    @DisplayName("agregar/quitar líneas (ADMIN) mantiene coherentes stock y totales de la venta")
    void detallesMantienenCoherencia() throws Exception {
        String admin = tokenAdmin();
        long categoria = crearCategoria(admin);
        long a = crearProducto(admin, categoria, 10, "10.00");
        long b = crearProducto(admin, categoria, 10, "2.50");
        Sesion ana = registrarUsuario();
        long ventaId = json(post("/api/v1/ventas", ana.token(), venta(a, 2))
                .andExpect(status().isCreated())).get("id").asLong();

        post("/api/v1/backoffice/ventas/" + ventaId + "/detalles", ana.token(), Map.of("productoId", b, "cantidad", 4))
                .andExpect(status().isForbidden());

        long detalleId = json(post("/api/v1/backoffice/ventas/" + ventaId + "/detalles", admin,
                Map.of("productoId", b, "cantidad", 4)).andExpect(status().isCreated())).get("id").asLong();

        assertEquals(6, stockEnBD(b));
        assertEquals(0, new java.math.BigDecimal("30.00").compareTo(jdbc.queryForObject(
                "select total from ventas where id = ?", java.math.BigDecimal.class, ventaId)));
        assertEquals(6, jdbc.queryForObject("select cantidad_items from ventas where id = ?", Integer.class, ventaId));

        delete("/api/v1/backoffice/detalles/" + detalleId, admin).andExpect(status().isNoContent());

        assertEquals(10, stockEnBD(b));
        assertEquals(0, new java.math.BigDecimal("20.00").compareTo(jdbc.queryForObject(
                "select total from ventas where id = ?", java.math.BigDecimal.class, ventaId)));
        assertEquals(2, jdbc.queryForObject("select cantidad_items from ventas where id = ?", Integer.class, ventaId));
    }

    @Test
    @DisplayName("no se puede borrar un producto con ventas asociadas: 409 (antes era 500)")
    void borrarProductoConVentas() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion ana = registrarUsuario();
        post("/api/v1/ventas", ana.token(), venta(productoId, 1)).andExpect(status().isCreated());

        delete("/api/v1/backoffice/productos/" + productoId, admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("CONCURRENCIA: 12 compras simultáneas de 1 unidad con stock 5 -> exactamente 5 exitosas, stock 0, sin sobreventa")
    void sinSobreventaBajoConcurrencia() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        int compradores = 12;
        List<Sesion> sesiones = new ArrayList<>();
        for (int i = 0; i < compradores; i++) {
            sesiones.add(registrarUsuario());
        }

        ExecutorService pool = Executors.newFixedThreadPool(compradores);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Integer>> resultados = new ArrayList<>();
        for (Sesion s : sesiones) {
            resultados.add(pool.submit(() -> {
                salida.await(); // todas las compras arrancan a la vez
                return post("/api/v1/ventas", s.token(), venta(productoId, 1))
                        .andReturn().getResponse().getStatus();
            }));
        }
        salida.countDown();

        int exitosas = 0;
        int rechazadas = 0;
        for (Future<Integer> f : resultados) {
            int codigo = f.get(60, TimeUnit.SECONDS);
            if (codigo == 201) {
                exitosas++;
            } else if (codigo == 400) {
                rechazadas++;
            }
        }
        pool.shutdown();

        assertEquals(5, exitosas, "solo debían poder comprar 5 (el stock inicial)");
        assertEquals(compradores - 5, rechazadas);
        assertEquals(0, stockEnBD(productoId));
        assertEquals(5, jdbc.queryForObject(
                "select coalesce(sum(cantidad), 0) from detalle_ventas where producto_id = ?",
                Integer.class, productoId));
    }
}
