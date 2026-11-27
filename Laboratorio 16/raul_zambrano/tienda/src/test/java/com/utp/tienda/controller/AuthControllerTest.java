package com.utp.tienda.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.JwtConfig;
import com.utp.tienda.config.SecurityConfig;
import com.utp.tienda.security.TokenService;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtConfig.class })
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private TokenService tokenService;

	@Test
	void loginValido_debeDevolverToken() throws Exception {
		var auth = UsernamePasswordAuthenticationToken.authenticated(
				"usuario",
				null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		when(authenticationManager.authenticate(any())).thenReturn(auth);
		when(tokenService.crearToken(auth)).thenReturn("jwt-de-prueba");
		when(tokenService.expiresInSeconds()).thenReturn(1800L);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"usuario","password":"Usuario123*"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").value("jwt-de-prueba"))
				.andExpect(jsonPath("$.expiresInSeconds").value(1800));
	}

	@Test
	void loginInvalido_debeResponder401() throws Exception {
		when(authenticationManager.authenticate(any()))
				.thenThrow(new BadCredentialsException("bad"));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"usuario","password":"incorrecta"}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Credenciales inválidas"));
	}
}
