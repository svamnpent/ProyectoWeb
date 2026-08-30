package com.utp.semana3.controller;

import com.utp.semana3.model.Producto;
import com.utp.semana3.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void registrar_debeRetornarProductoCreado() throws Exception {
        Producto productoRegistrado = new Producto(1L, "Laptop", 3500.00, 10);
        when(productoService.registrar(org.mockito.ArgumentMatchers.any(Producto.class)))
                .thenReturn(productoRegistrado);
        String json = """
                {
                "nombre": "Laptop",
                "precio": 3500.00,
                "stock": 10
                }
                """;
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/productos")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
public Producto registrar(@RequestBody Producto producto) {
return productoService.registrar(producto);
}
@Test
void buscarPorIdCuandoNoExiste_debeRetornar404() throws Exception {
when(productoService.buscarPorId(99L)).thenReturn(java.util.Optional.empty());
mockMvc.perform(get("/productos/99"))
.andExpect(status().isNotFound());
}
}
