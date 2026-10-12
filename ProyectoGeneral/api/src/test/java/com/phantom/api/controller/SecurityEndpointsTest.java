package com.phantom.api.controller;

import com.phantom.api.exception.GlobalExceptionHandler;
import com.phantom.api.service.*;
import com.phantom.api.support.WebMvcSecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static com.phantom.api.support.JwtTestUtils.admin;
import static com.phantom.api.support.JwtTestUtils.user;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autorización de TODOS los endpoints con la seguridad real:
 * qué estado HTTP obtiene un anónimo, un usuario normal (ID 5) y un ADMIN.
 * Los servicios están simulados: aquí solo se prueba la capa de seguridad.
 */
@WebMvcTest
@Import({GlobalExceptionHandler.class, WebMvcSecurityTestConfig.class})
class SecurityEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CategoriaService categoriaService;
    @MockBean private DetalleVentaService detalleVentaService;
    @MockBean private EtiquetaService etiquetaService;
    @MockBean private PerfilService perfilService;
    @MockBean private ProductoService productoService;
    @MockBean private UsuarioService usuarioService;
    @MockBean private VentaService ventaService;
    @MockBean private AuthService authService;

    private static final String PRODUCTO = "{\"nombre\":\"Laptop\",\"precio\":10.50,\"stock\":3,\"categoriaId\":1}";
    private static final String CATEGORIA = "{\"nombre\":\"Hogar\"}";
    private static final String ETIQUETA = "{\"nombre\":\"oferta\"}";
    private static final String USUARIO =
            "{\"nombre\":\"Ana\",\"email\":\"ana@x.com\",\"password\":\"Secreta123\"}";
    private static final String VENTA = "{\"detalles\":[{\"productoId\":1,\"cantidad\":2}]}";
    private static final String DETALLE = "{\"productoId\":1,\"cantidad\":1}";
    private static final String PERFIL = "{\"biografia\":\"hola\",\"usuarioId\":5}";
    private static final String LOGIN = "{\"email\":\"ana@x.com\",\"password\":\"Secreta123\"}";

    /** descripción, método, ruta, cuerpo, status anónimo, status usuario(5), status admin */
    private static Arguments caso(HttpMethod m, String ruta, String cuerpo, int anonimo, int usuario, int admin) {
        return Arguments.of(m.name() + " " + ruta, m, ruta, cuerpo, anonimo, usuario, admin);
    }

    static Stream<Arguments> casos() {
        HttpMethod G = HttpMethod.GET, P = HttpMethod.POST, U = HttpMethod.PUT, D = HttpMethod.DELETE;
        return Stream.of(
                // Catálogo: lectura pública
                caso(G, "/api/v1/productos", null, 200, 200, 200),
                caso(G, "/api/v1/productos/1", null, 200, 200, 200),
                caso(G, "/api/v1/productos/buscar?keyword=x", null, 200, 200, 200),
                caso(G, "/api/v1/categorias", null, 200, 200, 200),
                caso(G, "/api/v1/categorias/1", null, 200, 200, 200),
                caso(G, "/api/v1/etiquetas", null, 200, 200, 200),
                // Catálogo: escritura solo ADMIN
                caso(P, "/api/v1/productos", PRODUCTO, 401, 403, 201),
                caso(U, "/api/v1/productos/1", PRODUCTO, 401, 403, 200),
                caso(D, "/api/v1/productos/1", null, 401, 403, 204),
                caso(P, "/api/v1/categorias", CATEGORIA, 401, 403, 201),
                caso(U, "/api/v1/categorias/1", CATEGORIA, 401, 403, 200),
                caso(D, "/api/v1/categorias/1", null, 401, 403, 204),
                caso(P, "/api/v1/etiquetas", ETIQUETA, 401, 403, 201),
                caso(U, "/api/v1/etiquetas/1", ETIQUETA, 401, 403, 200),
                caso(D, "/api/v1/etiquetas/1", null, 401, 403, 204),
                // Usuarios: listado/búsqueda/alta/baja solo ADMIN; lectura y edición: uno mismo o ADMIN
                caso(G, "/api/v1/usuarios", null, 401, 403, 200),
                caso(G, "/api/v1/usuarios/buscar?email=a@b.com", null, 401, 403, 200),
                caso(P, "/api/v1/usuarios", USUARIO, 401, 403, 201),
                caso(G, "/api/v1/usuarios/5", null, 401, 200, 200),
                caso(G, "/api/v1/usuarios/6", null, 401, 403, 200),
                caso(U, "/api/v1/usuarios/5", USUARIO, 401, 200, 200),
                caso(U, "/api/v1/usuarios/6", USUARIO, 401, 403, 200),
                caso(D, "/api/v1/usuarios/5", null, 401, 403, 204),
                // Perfiles
                caso(G, "/api/v1/perfiles", null, 401, 403, 200),
                caso(G, "/api/v1/perfiles/1", null, 401, 200, 200),
                caso(P, "/api/v1/perfiles", PERFIL, 401, 201, 201),
                caso(U, "/api/v1/perfiles/1", PERFIL, 401, 200, 200),
                caso(D, "/api/v1/perfiles/1", null, 401, 204, 204),
                // Ventas
                caso(G, "/api/v1/ventas", null, 401, 403, 200),
                caso(G, "/api/v1/ventas/mias", null, 401, 200, 200),
                caso(G, "/api/v1/ventas/1", null, 401, 200, 200),
                caso(P, "/api/v1/ventas", VENTA, 401, 201, 201),
                caso(D, "/api/v1/ventas/1", null, 401, 403, 204),
                // Detalles
                caso(G, "/api/v1/ventas/1/detalles", null, 401, 200, 200),
                caso(P, "/api/v1/ventas/1/detalles", DETALLE, 401, 403, 201),
                caso(D, "/api/v1/detalles/1", null, 401, 403, 204),
                // Auth
                caso(P, "/api/v1/auth/login", LOGIN, 200, 200, 200),
                caso(P, "/api/v1/auth/register",
                        "{\"nombre\":\"Ana\",\"email\":\"ana@x.com\",\"password\":\"Secreta123\"}", 201, 201, 201),
                caso(G, "/api/v1/auth/me", null, 401, 200, 200));
    }

    private int ejecutar(HttpMethod metodo, String ruta, String cuerpo, RequestPostProcessor identidad)
            throws Exception {
        MockHttpServletRequestBuilder req = request(metodo, ruta);
        if (cuerpo != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        if (identidad != null) {
            req.with(identidad);
        }
        return mockMvc.perform(req).andReturn().getResponse().getStatus();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("Matriz de permisos por endpoint")
    void matrizDePermisos(String descripcion, HttpMethod metodo, String ruta, String cuerpo,
                          int anonimo, int usuario, int admin) throws Exception {
        assertEquals(anonimo, ejecutar(metodo, ruta, cuerpo, null), descripcion + " como ANÓNIMO");
        assertEquals(usuario, ejecutar(metodo, ruta, cuerpo, user(5)), descripcion + " como USUARIO(5)");
        assertEquals(admin, ejecutar(metodo, ruta, cuerpo, admin()), descripcion + " como ADMIN");
    }

    @Test
    @DisplayName("Un token inválido recibe 401 en JSON (no 500, no HTML)")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/mias").header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("Una ruta desconocida sin autenticar devuelve 401 (no revela qué rutas existen)")
    void rutaDesconocidaSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/no-existe")).andExpect(status().isUnauthorized());
    }
}
