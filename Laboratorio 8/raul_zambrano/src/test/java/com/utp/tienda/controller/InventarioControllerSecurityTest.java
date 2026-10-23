package com.utp.tienda.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.SecurityConfig;
import com.utp.tienda.service.ProductoService;

@WebMvcTest(InventarioController.class)
@Import(SecurityConfig.class)
class InventarioControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductoService productoService;

	@Test
	void listarInventarioSinAutenticacion_debeResponder401() throws Exception {
		mockMvc.perform(get("/api/inventario"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void ajustarComoUser_debeResponder403() throws Exception {
		mockMvc.perform(post("/api/inventario/ajustes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"productoId":1,"tipo":"ENTRADA","cantidad":2}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	void listarInventarioComoUser_debeResponder200() throws Exception {
		mockMvc.perform(get("/api/inventario"))
				.andExpect(status().isOk());
	}
}
