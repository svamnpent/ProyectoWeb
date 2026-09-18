package com.utp.productosapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.productosapi.model.Producto;
import com.utp.productosapi.service.ProductoService;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductoService service;

	@Test
	void postDebeRetornar201() throws Exception {
		when(service.crear(any(Producto.class)))
				.thenReturn(new Producto(1L, "Laptop Lenovo", 3500.0, 10, "Computo"));

		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"Laptop Lenovo","precio":3500,"stock":10,"categoria":"Computo"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nombre").value("Laptop Lenovo"))
				.andExpect(jsonPath("$.categoria").value("Computo"));
	}
}
