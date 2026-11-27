package com.utp.tienda.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.JwtConfig;
import com.utp.tienda.config.SecurityConfig;
import com.utp.tienda.dto.ResumenBackendResponse;
import com.utp.tienda.service.AdminResumenService;

@WebMvcTest(AdminController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
class AdminControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdminResumenService service;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void resumenSinToken_debeResponder401() throws Exception {
		mockMvc.perform(get("/api/admin/resumen"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminPuedeConsultarResumen() throws Exception {
		when(service.obtenerResumen())
				.thenReturn(new ResumenBackendResponse(5, 1, 3, "BACKEND_OPERATIVO"));

		mockMvc.perform(get("/api/admin/resumen")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.andExpect(status().isOk());
	}

	@Test
	void userNoPuedeConsultarResumen() throws Exception {
		mockMvc.perform(get("/api/admin/resumen")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
				.andExpect(status().isForbidden());
	}
}
