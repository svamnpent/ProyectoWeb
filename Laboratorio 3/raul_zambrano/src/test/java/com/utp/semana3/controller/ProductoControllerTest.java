package com.utp.semana3.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.semana3.model.Producto;
import com.utp.semana3.service.ProductoService;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void listar_debeRetornarProductosEnJson() throws Exception {
        when(productoService.listar()).thenReturn(List.of(
                new Producto(1L, "Laptop", 3500.00, 10),
                new Producto(2L, "Mouse", 80.00, 20)));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$[1].nombre").value("Mouse"));
    }

    @Test
    void registrar_debeRetornar201ConProductoCreado() throws Exception {
        when(productoService.registrar(any(Producto.class)))
                .thenReturn(new Producto(1L, "Laptop", 3500.00, 10));

        String json = "{\"nombre\":\"Laptop\",\"precio\":3500.00,\"stock\":10}";

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    void buscarPorId_existente_debeRetornar200() throws Exception {
        when(productoService.buscarPorId(1L))
                .thenReturn(Optional.of(new Producto(1L, "Laptop", 3500.00, 10)));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    void buscarPorId_inexistente_debeRetornar404() throws Exception {
        when(productoService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound());
    }
}

