package com.utp.tienda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(
			UserDetailsService users, PasswordEncoder passwords) {
		var provider = new DaoAuthenticationProvider(passwords);
		provider.setUserDetailsService(users);
		return new ProviderManager(provider);
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		var authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("roles");
		authorities.setAuthorityPrefix("");
		var converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
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
						.requestMatchers("/api/admin/**")
						.hasRole("ADMIN")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
		return http.build();
	}
}
