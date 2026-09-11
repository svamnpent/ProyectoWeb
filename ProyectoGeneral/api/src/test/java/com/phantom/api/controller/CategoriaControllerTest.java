package com.phantom.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.request.CategoriaRequestDTO;
import com.phantom.api.exception.GlobalExceptionHandler;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@Import(GlobalExceptionHandler.class) // Asegura cargar tu manejador global de excepciones
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de seguridad si usas Spring Security
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
    @DisplayName("GET /api/v1/categorias - Debe retornar lista de categorías (HTTP 200)")
    void obtenerTodas_DebeRetornarListaDeCategorias() throws Exception {
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
    @DisplayName("GET /api/v1/categorias/{id} - Debe retornar categoría por ID (HTTP 200)")
    void obtenerPorId_CuandoExiste_DebeRetornarCategoria() throws Exception {
        when(categoriaService.obtenerPorId(1L)).thenReturn(categoriaDTO);

        mockMvc.perform(get("/api/v1/categorias/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Electrónica")));

        verify(categoriaService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/v1/categorias/{id} - Debe retornar HTTP 404 al no encontrar recurso")
    void obtenerPorId_CuandoNoExiste_DebeRetornar404() throws Exception {
        when(categoriaService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Categoría no encontrada con el ID: 99"));

        mockMvc.perform(get("/api/v1/categorias/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Categoría no encontrada con el ID: 99")));

        verify(categoriaService, times(1)).obtenerPorId(99L);
    }

    @Test
    @DisplayName("POST /api/v1/categorias - Debe crear categoría (HTTP 201)")
    void crear_ConDatosValidos_DebeRetornar201() throws Exception {
        when(categoriaService.crear(any(CategoriaRequestDTO.class))).thenReturn(categoriaDTO);

        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Electrónica")));

        verify(categoriaService, times(1)).crear(any(CategoriaRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/categorias - Debe fallar validación @Valid y retornar HTTP 400")
    void crear_ConNombreEnBlanco_DebeRetornar400() throws Exception {
        CategoriaRequestDTO requestInvalido = CategoriaRequestDTO.builder()
                .nombre("") // Dispara @NotBlank
                .descripcion("Descripción válida")
                .build();

        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validación Fallida")));

        verify(categoriaService, never()).crear(any());
    }

    @Test
    @DisplayName("PUT /api/v1/categorias/{id} - Debe actualizar categoría (HTTP 200)")
    void actualizar_ConDatosValidos_DebeRetornar200() throws Exception {
        when(categoriaService.actualizar(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(categoriaDTO);

        mockMvc.perform(put("/api/v1/categorias/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Electrónica")));

        verify(categoriaService, times(1)).actualizar(eq(1L), any(CategoriaRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/categorias/{id} - Debe eliminar categoría (HTTP 204)")
    void eliminar_CuandoExiste_DebeRetornar204() throws Exception {
        doNothing().when(categoriaService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/categorias/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).eliminar(1L);
    }
}