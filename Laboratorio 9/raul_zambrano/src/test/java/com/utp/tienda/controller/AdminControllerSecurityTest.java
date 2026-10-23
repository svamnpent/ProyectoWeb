package com.utp.tienda.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.JwtConfig;
import com.utp.tienda.config.SecurityConfig;
import com.utp.tienda.service.AdminService;

@WebMvcTest(AdminController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
class AdminControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdminService adminService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void reporteSinToken_debeResponder401() throws Exception {
		mockMvc.perform(get("/api/admin/reporte"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void reporteComoUser_debeResponder403() throws Exception {
		mockMvc.perform(get("/api/admin/reporte"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void reporteComoAdmin_debeResponder200() throws Exception {
		mockMvc.perform(get("/api/admin/reporte"))
				.andExpect(status().isOk());
	}
}
