package com.utp.semana4_api_rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.semana4_api_rest.dto.ActualizarStockRequest;
import com.utp.semana4_api_rest.dto.ProductoRequest;
import com.utp.semana4_api_rest.model.Producto;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void testGetAll() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @Order(2)
    void testCreate() throws Exception {
        ProductoRequest request = new ProductoRequest("Teclado", "Tecnologia", 120.0, 15);
        MvcResult result = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        Producto created = objectMapper.readValue(body, Producto.class);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getNombre()).isEqualTo("Teclado");
    }
}
