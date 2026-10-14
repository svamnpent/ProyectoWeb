package com.phantom.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.request.CategoriaRequestDTO;
import com.phantom.api.exception.GlobalExceptionHandler;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.service.CategoriaService;
import com.phantom.api.support.WebMvcSecurityTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.phantom.api.support.JwtTestUtils.admin;
import static com.phantom.api.support.JwtTestUtils.user;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Ahora con la seguridad real activa (antes los filtros estaban desactivados):
 * lecturas públicas, escritura solo ADMIN.
 */
@WebMvcTest(CategoriaController.class)
@Import({GlobalExceptionHandler.class, WebMvcSecurityTestConfig.class})
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    private CategoriaDTO categoriaDTO;
    private CategoriaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        categoriaDTO = CategoriaDTO.builder()
                .id(1L)
                .nombre("Electrónica")
                .descripcion("Dispositivos y gadgets")
                .build();

        requestDTO = CategoriaRequestDTO.builder()
                .nombre("Electrónica")
                .descripcion("Dispositivos y gadgets")
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/categorias - público, retorna lista (HTTP 200)")
    void obtenerTodas_esPublico() throws Exception {
        when(categoriaService.obtenerTodas()).thenReturn(List.of(categoriaDTO));

        mockMvc.perform(get("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nombre", is("Electrónica")));

        verify(categoriaService, times(1)).obtenerTodas();
    }

    @Test
    @DisplayName("GET /api/v1/categorias/{id} - público, retorna categoría (HTTP 200)")
    void obtenerPorId_CuandoExiste() throws Exception {
        when(categoriaService.obtenerPorId(1L)).thenReturn(categoriaDTO);

        mockMvc.perform(get("/api/v1/categorias/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Electrónica")));
    }

    @Test
    @DisplayName("GET /api/v1/categorias/{id} - HTTP 404 al no encontrar recurso")
    void obtenerPorId_CuandoNoExiste() throws Exception {
        when(categoriaService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada con el ID: 99"));

        mockMvc.perform(get("/api/v1/categorias/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Categoría no encontrada con el ID: 99")));
    }

    @Test
    @DisplayName("POST /api/v1/categorias - ADMIN crea categoría (HTTP 201)")
    void crear_comoAdmin() throws Exception {
        when(categoriaService.crear(any(CategoriaRequestDTO.class))).thenReturn(categoriaDTO);

        mockMvc.perform(post("/api/v1/categorias")
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Electrónica")));

        verify(categoriaService, times(1)).crear(any(CategoriaRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/categorias - sin token: HTTP 401 y no llega al servicio")
    void crear_sinToken() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        verifyNoInteractions(categoriaService);
    }

    @Test
    @DisplayName("POST /api/v1/categorias - usuario normal: HTTP 403 y no llega al servicio")
    void crear_comoUsuarioNormal() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                .with(user(5))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        verifyNoInteractions(categoriaService);
    }

    @Test
    @DisplayName("POST /api/v1/categorias - falla validación @Valid (HTTP 400)")
    void crear_ConNombreEnBlanco() throws Exception {
        CategoriaRequestDTO requestInvalido = CategoriaRequestDTO.builder()
                .nombre("")
                .descripcion("Descripción válida")
                .build();

        mockMvc.perform(post("/api/v1/categorias")
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validación Fallida")));

        verify(categoriaService, never()).crear(any());
    }

    @Test
    @DisplayName("PUT /api/v1/categorias/{id} - ADMIN actualiza (HTTP 200)")
    void actualizar_comoAdmin() throws Exception {
        when(categoriaService.actualizar(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(categoriaDTO);

        mockMvc.perform(put("/api/v1/categorias/{id}", 1L)
                .with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("DELETE /api/v1/categorias/{id} - ADMIN elimina (HTTP 204)")
    void eliminar_comoAdmin() throws Exception {
        doNothing().when(categoriaService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/categorias/{id}", 1L).with(admin()))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/categorias/{id} - usuario normal: HTTP 403")
    void eliminar_comoUsuarioNormal() throws Exception {
        mockMvc.perform(delete("/api/v1/categorias/{id}", 1L).with(user(5)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
    }
}
