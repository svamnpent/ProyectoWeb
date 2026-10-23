package com.utp.tienda.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.SecurityConfig;

@WebMvcTest(PublicoController.class)
@Import(SecurityConfig.class)
class PublicoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void estadoSinAutenticacion_debeResponder200() throws Exception {
		mockMvc.perform(get("/api/publico/estado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("API disponible"));
	}
}
