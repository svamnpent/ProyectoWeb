package com.phantom.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo de autenticación y autorización contra la aplicación real y PostgreSQL.
 */
class AuthFlowIT extends AbstractIntegrationIT {

    @Test
    @DisplayName("registro -> login -> /me, sin exponer el password y con rol USER")
    void registroLoginYMe() throws Exception {
        String email = unico("flow") + "@test.local";

        post("/api/v1/auth/register", null, Map.of("nombre", "Ana", "email", email, "password", PASSWORD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.usuario.rol", is("USER")))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());

        String token = json(post("/api/v1/auth/login", null, Map.of("email", email.toUpperCase(), "password", PASSWORD))
                .andExpect(status().isOk())).get("accessToken").asText();

        get("/api/v1/auth/me", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("el password se guarda con BCrypt, nunca en texto plano")
    void passwordHasheadoEnBD() throws Exception {
        Sesion s = registrarUsuario();

        String hash = jdbc.queryForObject("select password from usuarios where id = ?", String.class, s.id());

        assertTrue(hash.startsWith("$2"), hash);
        assertTrue(!hash.contains(PASSWORD));
    }

    @Test
    @DisplayName("registro: un 'rol' enviado por el cliente se ignora (no se puede crear un ADMIN)")
    void registroNoPermiteElegirRol() throws Exception {
        String email = unico("hack") + "@test.local";

        post("/api/v1/auth/register", null,
                Map.of("nombre", "Hacker", "email", email, "password", PASSWORD, "rol", "ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol", is("USER")));
    }

    @Test
    @DisplayName("registro: email duplicado (sin distinguir mayúsculas) -> 400; contraseña corta -> 400")
    void registroValidaciones() throws Exception {
        Sesion s = registrarUsuario();

        post("/api/v1/auth/register", null,
                Map.of("nombre", "Otro", "email", s.email().toUpperCase(), "password", PASSWORD))
                .andExpect(status().isBadRequest());

        post("/api/v1/auth/register", null,
                Map.of("nombre", "Otro", "email", unico("x") + "@test.local", "password", "corta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validación Fallida")));
    }

    @Test
    @DisplayName("login con password incorrecto o email inexistente -> 401 con el mismo mensaje")
    void loginFallido() throws Exception {
        Sesion s = registrarUsuario();

        post("/api/v1/auth/login", null, Map.of("email", s.email(), "password", "incorrecta123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Credenciales inválidas")));

        post("/api/v1/auth/login", null, Map.of("email", unico("nadie") + "@test.local", "password", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Credenciales inválidas")));
    }

    @Test
    @DisplayName("tras 3 intentos fallidos la cuenta se bloquea (429) incluso con la contraseña correcta")
    void bloqueoPorFuerzaBruta() throws Exception {
        Sesion s = registrarUsuario();

        for (int i = 0; i < 3; i++) {
            post("/api/v1/auth/login", null, Map.of("email", s.email(), "password", "incorrecta123"))
                    .andExpect(status().isUnauthorized());
        }

        post("/api/v1/auth/login", null, Map.of("email", s.email(), "password", PASSWORD))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("endpoints protegidos: sin token o con token inválido -> 401 en JSON")
    void sinTokenOTokenInvalido() throws Exception {
        get("/api/v1/auth/me", null).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
        get("/api/v1/ventas/mias", "token.invalido.xyz").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("catálogo: lectura pública, escritura solo ADMIN")
    void catalogoPublicoEscrituraAdmin() throws Exception {
        Sesion s = registrarUsuario();
        String admin = tokenAdmin();

        get("/api/v1/productos", null).andExpect(status().isOk());
        get("/api/v1/categorias", null).andExpect(status().isOk());

        post("/api/v1/categorias", null, Map.of("nombre", unico("cat"))).andExpect(status().isUnauthorized());
        post("/api/v1/categorias", s.token(), Map.of("nombre", unico("cat"))).andExpect(status().isForbidden());
        post("/api/v1/categorias", admin, Map.of("nombre", unico("cat"))).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("usuarios: solo se puede ver/editar el propio perfil; el listado es solo ADMIN")
    void accesoARecursosDeUsuarios() throws Exception {
        Sesion ana = registrarUsuario();
        Sesion luis = registrarUsuario();

        get("/api/v1/usuarios/" + ana.id(), ana.token()).andExpect(status().isOk());
        get("/api/v1/usuarios/" + luis.id(), ana.token()).andExpect(status().isForbidden());
        get("/api/v1/usuarios", ana.token()).andExpect(status().isForbidden());
        get("/api/v1/usuarios", tokenAdmin()).andExpect(status().isOk());
    }

    @Test
    @DisplayName("escalada de privilegios: un usuario NO puede asignarse el rol ADMIN con PUT")
    void noPuedeAutoAsignarseAdmin() throws Exception {
        Sesion s = registrarUsuario();

        put("/api/v1/usuarios/" + s.id(), s.token(), Map.of(
                "nombre", "Usuario Test", "email", s.email(), "password", PASSWORD, "rol", "ADMIN"))
                .andExpect(status().isForbidden());

        String rol = jdbc.queryForObject("select rol from usuarios where id = ?", String.class, s.id());
        org.junit.jupiter.api.Assertions.assertEquals("USER", rol);
    }

    @Test
    @DisplayName("un usuario puede actualizar sus propios datos y el nuevo password se hashea")
    void actualizarPropiosDatos() throws Exception {
        Sesion s = registrarUsuario();

        put("/api/v1/usuarios/" + s.id(), s.token(), Map.of(
                "nombre", "Nombre Nuevo", "email", s.email(), "password", "NuevaClave12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Nombre Nuevo")));

        post("/api/v1/auth/login", null, Map.of("email", s.email(), "password", "NuevaClave12345"))
                .andExpect(status().isOk());
        post("/api/v1/auth/login", null, Map.of("email", s.email(), "password", PASSWORD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("perfiles: solo el dueño (o ADMIN) puede crear y ver su perfil")
    void perfilesPorDueno() throws Exception {
        Sesion ana = registrarUsuario();
        Sesion luis = registrarUsuario();

        // Luis intenta crear un perfil para Ana
        post("/api/v1/perfiles", luis.token(), Map.of("usuarioId", ana.id(), "biografia", "intruso"))
                .andExpect(status().isForbidden());

        long perfilId = json(post("/api/v1/perfiles", ana.token(),
                Map.of("usuarioId", ana.id(), "biografia", "hola")).andExpect(status().isCreated()))
                .get("id").asLong();

        get("/api/v1/perfiles/" + perfilId, ana.token()).andExpect(status().isOk());
        get("/api/v1/perfiles/" + perfilId, luis.token()).andExpect(status().isForbidden());
        get("/api/v1/perfiles/" + perfilId, tokenAdmin()).andExpect(status().isOk());
    }
}
