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

import java.util.List;
import java.util.stream.Stream;

import static com.phantom.api.support.JwtTestUtils.admin;
import static com.phantom.api.support.JwtTestUtils.trabajador;
import static com.phantom.api.support.JwtTestUtils.user;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autorización de TODOS los endpoints con la seguridad real, separada en las dos zonas
 * de la API (TIENDA y BACK-OFFICE): qué estado HTTP obtiene un anónimo, un comprador (ID 5),
 * un trabajador (ID 2) y un ADMIN (ID 1).
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

    /** método, ruta, cuerpo y status esperado para: anónimo, comprador (ID 5), trabajador (ID 2), admin (ID 1). */
    private static Arguments caso(HttpMethod m, String ruta, String cuerpo,
                                  int anonimo, int comprador, int trabajador, int admin) {
        return Arguments.of(m.name() + " " + ruta, m, ruta, cuerpo, anonimo, comprador, trabajador, admin);
    }

    static Stream<Arguments> casos() {
        HttpMethod G = HttpMethod.GET, P = HttpMethod.POST, U = HttpMethod.PUT, D = HttpMethod.DELETE;
        String BO = "/api/v1/backoffice";
        return Stream.of(
                // ===================== TIENDA: catálogo público (lectura)
                caso(G, "/api/v1/productos", null, 200, 200, 200, 200),
                caso(G, "/api/v1/productos/1", null, 200, 200, 200, 200),
                caso(G, "/api/v1/productos/buscar?keyword=x", null, 200, 200, 200, 200),
                caso(G, "/api/v1/categorias", null, 200, 200, 200, 200),
                caso(G, "/api/v1/categorias/1", null, 200, 200, 200, 200),
                caso(G, "/api/v1/categorias/buscar?keyword=x", null, 200, 200, 200, 200),
                caso(G, "/api/v1/etiquetas", null, 200, 200, 200, 200),
                caso(G, "/api/v1/etiquetas/1", null, 200, 200, 200, 200),
                // La escritura del catálogo YA NO existe en la zona pública (ni para el ADMIN)
                caso(P, "/api/v1/productos", PRODUCTO, 401, 405, 405, 405),
                caso(D, "/api/v1/categorias/1", null, 401, 405, 405, 405),
                caso(D, "/api/v1/ventas/1", null, 401, 405, 405, 405),

                // ===================== TIENDA: autenticación
                caso(P, "/api/v1/auth/login", LOGIN, 200, 200, 200, 200),
                caso(P, "/api/v1/auth/register",
                        "{\"nombre\":\"Ana\",\"email\":\"ana@x.com\",\"password\":\"Secreta123\"}", 201, 201, 201, 201),
                caso(G, "/api/v1/auth/me", null, 401, 200, 200, 200),

                // ===================== TIENDA: la cuenta del propio comprador
                caso(G, "/api/v1/usuarios/5", null, 401, 200, 403, 403),
                caso(G, "/api/v1/usuarios/6", null, 401, 403, 403, 403),
                caso(U, "/api/v1/usuarios/5", USUARIO, 401, 200, 403, 403),
                caso(U, "/api/v1/usuarios/6", USUARIO, 401, 403, 403, 403),
                caso(G, "/api/v1/perfiles/1", null, 401, 200, 200, 200),
                caso(P, "/api/v1/perfiles", PERFIL, 401, 201, 201, 201),
                caso(U, "/api/v1/perfiles/1", PERFIL, 401, 200, 200, 200),
                caso(D, "/api/v1/perfiles/1", null, 401, 204, 204, 204),

                // ===================== TIENDA: compras del comprador
                caso(P, "/api/v1/ventas", VENTA, 401, 201, 201, 201),
                caso(G, "/api/v1/ventas/mias", null, 401, 200, 200, 200),
                caso(G, "/api/v1/ventas/1", null, 401, 200, 200, 200),
                caso(G, "/api/v1/ventas/1/detalles", null, 401, 200, 200, 200),

                // ===================== BACK-OFFICE: catálogo (crear/editar: personal; eliminar: ADMIN)
                caso(P, BO + "/productos", PRODUCTO, 401, 403, 201, 201),
                caso(U, BO + "/productos/1", PRODUCTO, 401, 403, 200, 200),
                caso(D, BO + "/productos/1", null, 401, 403, 403, 204),
                caso(P, BO + "/categorias", CATEGORIA, 401, 403, 201, 201),
                caso(U, BO + "/categorias/1", CATEGORIA, 401, 403, 200, 200),
                caso(D, BO + "/categorias/1", null, 401, 403, 403, 204),
                caso(P, BO + "/etiquetas", ETIQUETA, 401, 403, 201, 201),
                caso(U, BO + "/etiquetas/1", ETIQUETA, 401, 403, 200, 200),
                caso(D, BO + "/etiquetas/1", null, 401, 403, 403, 204),

                // ===================== BACK-OFFICE: ventas (ver/registrar: personal; anular: ADMIN)
                caso(G, BO + "/ventas", null, 401, 403, 200, 200),
                caso(G, BO + "/ventas/1", null, 401, 403, 200, 200),
                caso(P, BO + "/ventas", VENTA, 401, 403, 201, 201),
                caso(D, BO + "/ventas/1", null, 401, 403, 403, 204),
                caso(G, BO + "/ventas/1/detalles", null, 401, 403, 200, 200),
                caso(P, BO + "/ventas/1/detalles", DETALLE, 401, 403, 403, 201),
                caso(D, BO + "/detalles/1", null, 401, 403, 403, 204),

                // ===================== BACK-OFFICE: usuarios y perfiles (solo ADMIN)
                caso(G, BO + "/usuarios", null, 401, 403, 403, 200),
                caso(G, BO + "/usuarios/buscar?email=a@b.com", null, 401, 403, 403, 200),
                caso(G, BO + "/usuarios/5", null, 401, 403, 403, 200),
                caso(P, BO + "/usuarios", USUARIO, 401, 403, 403, 201),
                caso(U, BO + "/usuarios/5", USUARIO, 401, 403, 403, 200),
                caso(D, BO + "/usuarios/5", null, 401, 403, 403, 204),
                caso(G, BO + "/perfiles", null, 401, 403, 403, 200));
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
                          int anonimo, int comprador, int trabajador, int admin) throws Exception {
        assertEquals(anonimo, ejecutar(metodo, ruta, cuerpo, null), descripcion + " como ANÓNIMO");
        assertEquals(comprador, ejecutar(metodo, ruta, cuerpo, user(5)), descripcion + " como COMPRADOR(5)");
        assertEquals(trabajador, ejecutar(metodo, ruta, cuerpo, trabajador()), descripcion + " como TRABAJADOR");
        assertEquals(admin, ejecutar(metodo, ruta, cuerpo, admin()), descripcion + " como ADMIN");
    }

    @Test
    @DisplayName("TODO el back-office exige personal: un comprador recibe 403 y un anónimo 401, sin excepción")
    void todoElBackOfficeExigePersonal() throws Exception {
        for (String ruta : List.of("/productos/1", "/categorias/1", "/etiquetas/1", "/ventas", "/ventas/1",
                "/ventas/1/detalles", "/usuarios", "/usuarios/1", "/perfiles", "/detalles/1")) {
            for (HttpMethod m : List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)) {
                assertEquals(401, ejecutar(m, "/api/v1/backoffice" + ruta, "{}", null),
                        m + " " + ruta + " como ANÓNIMO");
                assertEquals(403, ejecutar(m, "/api/v1/backoffice" + ruta, "{}", user(5)),
                        m + " " + ruta + " como COMPRADOR");
            }
        }
    }

    @Test
    @DisplayName("Un token inválido recibe 401 en JSON (no 500, no HTML)")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/mias").header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                // también los errores de seguridad usan la hora de Perú
                .andExpect(jsonPath("$.timestamp", org.hamcrest.Matchers.endsWith("-05:00")));
    }

    @Test
    @DisplayName("Una ruta desconocida sin autenticar devuelve 401 (no revela qué rutas existen)")
    void rutaDesconocidaSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/no-existe")).andExpect(status().isUnauthorized());
    }
}
