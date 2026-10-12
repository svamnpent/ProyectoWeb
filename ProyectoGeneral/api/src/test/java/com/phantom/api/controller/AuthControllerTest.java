package com.phantom.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantom.api.dto.AuthResponseDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.LoginRequestDTO;
import com.phantom.api.dto.request.RegisterRequestDTO;
import com.phantom.api.exception.GlobalExceptionHandler;
import com.phantom.api.exception.TooManyRequestsException;
import com.phantom.api.service.AuthService;
import com.phantom.api.support.WebMvcSecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static com.phantom.api.support.JwtTestUtils.user;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, WebMvcSecurityTestConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthService authService;

    private AuthResponseDTO respuesta() {
        return AuthResponseDTO.builder().accessToken("TOKEN").tokenType("Bearer").expiresIn(3600)
                .usuario(UsuarioDTO.builder().id(1L).email("ana@x.com").rol("USER").build()).build();
    }

    @Test
    @DisplayName("POST /auth/register - público, HTTP 201 con token")
    void register_ok() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(respuesta());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequestDTO.builder()
                        .nombre("Ana").email("ana@x.com").password("Secreta123").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", is("TOKEN")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /auth/register - contraseña corta: HTTP 400")
    void register_passwordCorta() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequestDTO.builder()
                        .nombre("Ana").email("ana@x.com").password("123").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")));

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/register - un campo 'rol' enviado por el cliente se ignora")
    void register_ignoraRol() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(respuesta());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"email\":\"ana@x.com\",\"password\":\"Secreta123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /auth/login - credenciales válidas: HTTP 200")
    void login_ok() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(respuesta());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email("ana@x.com").password("Secreta123").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("TOKEN")));
    }

    @Test
    @DisplayName("POST /auth/login - credenciales inválidas: HTTP 401")
    void login_credencialesInvalidas() throws Exception {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email("ana@x.com").password("mala").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Credenciales inválidas")));
    }

    @Test
    @DisplayName("POST /auth/login - demasiados intentos: HTTP 429")
    void login_bloqueado() throws Exception {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new TooManyRequestsException("Demasiados intentos fallidos"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequestDTO.builder().email("ana@x.com").password("x").build())))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status", is(429)));
    }

    @Test
    @DisplayName("POST /auth/login - email con formato inválido: HTTP 400")
    void login_emailInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-es-email\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("GET /auth/me - requiere autenticación")
    void me() throws Exception {
        when(authService.me()).thenReturn(UsuarioDTO.builder().id(5L).email("ana@x.com").build());

        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/me").with(user(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));
    }
}
