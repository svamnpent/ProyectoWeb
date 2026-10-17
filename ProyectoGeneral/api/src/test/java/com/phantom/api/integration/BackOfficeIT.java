package com.phantom.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La zona BACK-OFFICE contra la aplicación real y PostgreSQL: qué puede y qué NO puede hacer
 * un TRABAJADOR frente a un ADMIN y frente a un comprador.
 */
class BackOfficeIT extends AbstractIntegrationIT {

    private static final String BO = "/api/v1/backoffice";

    @Test
    @DisplayName("el ADMIN da de alta a un trabajador y este inicia sesión con rol TRABAJADOR")
    void adminCreaTrabajador() throws Exception {
        Sesion trabajador = crearTrabajador();

        get("/api/v1/auth/me", trabajador.token())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol", is("TRABAJADOR")));
        assertEquals("TRABAJADOR", jdbc.queryForObject("select rol from usuarios where id = ?",
                String.class, trabajador.id()));
    }

    @Test
    @DisplayName("un trabajador crea y edita el catálogo, pero NO puede eliminarlo")
    void trabajadorGestionaCatalogo() throws Exception {
        Sesion trabajador = crearTrabajador();
        long categoriaId = json(post(BO + "/categorias", trabajador.token(), Map.of("nombre", unico("cat")))
                .andExpect(status().isCreated())).get("id").asLong();
        long productoId = crearProducto(trabajador.token(), categoriaId, 5, "10.00");

        put(BO + "/productos/" + productoId, trabajador.token(), Map.of(
                "nombre", "Editado por trabajador", "precio", 12.50, "stock", 7, "categoriaId", categoriaId))
                .andExpect(status().isOk());
        assertEquals(7, stockEnBD(productoId));

        delete(BO + "/productos/" + productoId, trabajador.token()).andExpect(status().isForbidden());
        delete(BO + "/categorias/" + categoriaId, trabajador.token()).andExpect(status().isForbidden());
        assertEquals(1, jdbc.queryForObject("select count(*) from productos where id = ?", Integer.class, productoId));

        delete(BO + "/productos/" + productoId, tokenAdmin()).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("venta de mostrador: el trabajador registra la compra de un cliente y se la ve en 'mis ventas'")
    void ventaDeMostradorParaUnCliente() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion cliente = registrarUsuario();
        Sesion trabajador = crearTrabajador();

        long ventaId = json(post(BO + "/ventas", trabajador.token(), Map.of(
                "usuarioId", cliente.id(),
                "detalles", List.of(Map.of("productoId", productoId, "cantidad", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.id", is((int) cliente.id())))
                // el personal ve solo id, nombre y email del cliente, no sus datos personales
                .andExpect(jsonPath("$.usuario.*", hasSize(3)))
                .andExpect(jsonPath("$.total", is(20.00)))).get("id").asLong();

        assertEquals(3, stockEnBD(productoId));
        assertEquals(cliente.id(), jdbc.queryForObject("select usuario_id from ventas where id = ?", Long.class, ventaId));

        get("/api/v1/ventas/mias", cliente.token()).andExpect(jsonPath("$", hasSize(1)));
        get("/api/v1/ventas/mias", trabajador.token()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("venta de mostrador sin cliente: queda sin usuario y descuenta stock")
    void ventaDeMostradorSinCliente() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion trabajador = crearTrabajador();

        long ventaId = json(post(BO + "/ventas", trabajador.token(), venta(productoId, 1))
                .andExpect(status().isCreated())).get("id").asLong();

        assertNull(jdbc.queryForObject("select usuario_id from ventas where id = ?", Long.class, ventaId));
        assertEquals(4, stockEnBD(productoId));
    }

    @Test
    @DisplayName("un comprador que usa la compra normal NO puede comprar a nombre de otro, aunque lo pida")
    void compradorNoCompraAOtroNombre() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion ana = registrarUsuario();
        Sesion luis = registrarUsuario();

        post("/api/v1/ventas", ana.token(), Map.of("usuarioId", luis.id(),
                "detalles", List.of(Map.of("productoId", productoId, "cantidad", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.id", is((int) ana.id())));

        // y la venta de mostrador le está vedada
        post(BO + "/ventas", ana.token(), venta(productoId, 1)).andExpect(status().isForbidden());
        assertEquals(4, stockEnBD(productoId));
    }

    @Test
    @DisplayName("el trabajador ve todas las ventas, pero no puede anular ni editar sus líneas (solo ADMIN)")
    void trabajadorVeVentasPeroNoLasAnula() throws Exception {
        String admin = tokenAdmin();
        long productoId = crearProducto(admin, crearCategoria(admin), 5, "10.00");
        Sesion cliente = registrarUsuario();
        Sesion trabajador = crearTrabajador();
        long ventaId = json(post("/api/v1/ventas", cliente.token(), venta(productoId, 2))
                .andExpect(status().isCreated())).get("id").asLong();

        get(BO + "/ventas", trabajador.token()).andExpect(status().isOk());
        get(BO + "/ventas/" + ventaId, trabajador.token()).andExpect(status().isOk());
        get(BO + "/ventas/" + ventaId + "/detalles", trabajador.token())
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        get("/api/v1/ventas/" + ventaId, trabajador.token()).andExpect(status().isOk()); // también por la ruta de la tienda

        delete(BO + "/ventas/" + ventaId, trabajador.token()).andExpect(status().isForbidden());
        post(BO + "/ventas/" + ventaId + "/detalles", trabajador.token(),
                Map.of("productoId", productoId, "cantidad", 1)).andExpect(status().isForbidden());
        assertEquals(3, stockEnBD(productoId));
        assertEquals(1, jdbc.queryForObject("select count(*) from ventas where id = ?", Integer.class, ventaId));

        delete(BO + "/ventas/" + ventaId, admin).andExpect(status().isNoContent());
        assertEquals(5, stockEnBD(productoId));
    }

    @Test
    @DisplayName("el trabajador NO accede a usuarios ni a datos personales (cuentas, perfiles)")
    void trabajadorNoVeDatosPersonales() throws Exception {
        Sesion cliente = registrarUsuario();
        Sesion trabajador = crearTrabajador();
        long perfilId = json(post("/api/v1/perfiles", cliente.token(),
                Map.of("usuarioId", cliente.id(), "biografia", "privado")).andExpect(status().isCreated()))
                .get("id").asLong();

        get(BO + "/usuarios", trabajador.token()).andExpect(status().isForbidden());
        get(BO + "/usuarios/" + cliente.id(), trabajador.token()).andExpect(status().isForbidden());
        get(BO + "/perfiles", trabajador.token()).andExpect(status().isForbidden());
        post(BO + "/usuarios", trabajador.token(), Map.of(
                "nombre", "X", "email", unico("x") + "@test.local", "password", PASSWORD, "rol", "ADMIN"))
                .andExpect(status().isForbidden());
        // ni por las rutas de la tienda
        get("/api/v1/usuarios/" + cliente.id(), trabajador.token()).andExpect(status().isForbidden());
        get("/api/v1/perfiles/" + perfilId, trabajador.token()).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un trabajador NO puede ascenderse a ADMIN editando su propia cuenta")
    void trabajadorNoSeAscienda() throws Exception {
        Sesion trabajador = crearTrabajador();

        put("/api/v1/usuarios/" + trabajador.id(), trabajador.token(), Map.of(
                "nombre", "Trabajador Test", "email", trabajador.email(), "password", PASSWORD, "rol", "ADMIN"))
                .andExpect(status().isForbidden());

        assertEquals("TRABAJADOR", jdbc.queryForObject("select rol from usuarios where id = ?",
                String.class, trabajador.id()));
    }

    @Test
    @DisplayName("el ADMIN asciende a un comprador a TRABAJADOR y, al volver a iniciar sesión, entra al back-office")
    void adminAsciendeAComprador() throws Exception {
        Sesion comprador = registrarUsuario();
        get(BO + "/ventas", comprador.token()).andExpect(status().isForbidden());

        put(BO + "/usuarios/" + comprador.id(), tokenAdmin(), Map.of(
                "nombre", "Usuario Test", "email", comprador.email(), "password", PASSWORD, "rol", "TRABAJADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol", is("TRABAJADOR")));

        String nuevoToken = json(post("/api/v1/auth/login", null,
                Map.of("email", comprador.email(), "password", PASSWORD))
                .andExpect(status().isOk())).get("accessToken").asText();
        get(BO + "/ventas", nuevoToken).andExpect(status().isOk());
    }

    @Test
    @DisplayName("el ADMIN conserva acceso total al back-office")
    void adminAccesoTotal() throws Exception {
        String admin = tokenAdmin();

        get(BO + "/usuarios", admin).andExpect(status().isOk());
        get(BO + "/perfiles", admin).andExpect(status().isOk());
        get(BO + "/ventas", admin).andExpect(status().isOk());
    }
}
