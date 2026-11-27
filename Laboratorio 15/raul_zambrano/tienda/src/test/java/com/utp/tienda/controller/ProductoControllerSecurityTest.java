package com.utp.tienda.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.JwtConfig;
import com.utp.tienda.config.SecurityConfig;
import com.utp.tienda.service.ProductoService;

@WebMvcTest(ProductoController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
class ProductoControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductoService productoService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void listarSinAutenticacion_debeResponder401() throws Exception {
		mockMvc.perform(get("/api/productos"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void listarComoUser_debeResponder200() throws Exception {
		mockMvc.perform(get("/api/productos"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "USER")
	void eliminarComoUser_debeResponder403() throws Exception {
		mockMvc.perform(delete("/api/productos/1"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	void crearComoUser_debeResponder403() throws Exception {
		mockMvc.perform(post("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"Monitor 27 pulgadas","categoria":"Tecnologia","precio":1250.00,"stock":8}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void eliminarComoAdmin_debeResponder204() throws Exception {
		mockMvc.perform(delete("/api/productos/1"))
				.andExpect(status().isNoContent());
	}
}
