package com.phantom.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantom.api.dto.CategoriaDTO;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Zona TIENDA: las categorías son de lectura pública. La escritura vive en el back-office
 * (ver BackOfficeControllerTest).
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

    @BeforeEach
    void setUp() {
        categoriaDTO = CategoriaDTO.builder()
                .id(1L)
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
    @DisplayName("GET /api/v1/categorias/buscar - público")
    void buscar() throws Exception {
        when(categoriaService.buscarPorNombre("elec")).thenReturn(List.of(categoriaDTO));

        mockMvc.perform(get("/api/v1/categorias/buscar").param("keyword", "elec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("la escritura ya NO existe en la zona pública (ni para un ADMIN): vive en /backoffice")
    void escrituraNoExisteEnLaTienda() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(java.util.Map.of("nombre", "Hogar"));

        mockMvc.perform(post("/api/v1/categorias").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/v1/categorias/1").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/v1/categorias/1").with(admin()))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(categoriaService);
    }
}
