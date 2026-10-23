package com.utp.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/publico/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/productos/**")
						.hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/productos/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/productos/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/productos/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/productos/**")
						.hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/inventario/**")
						.hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/inventario/ajustes")
						.hasRole("ADMIN")
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}
