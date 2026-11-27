package com.utp.tienda.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

class TokenServiceTest {

	@Test
	void tokenContieneUsuarioYRolFirmados() {
		byte[] raw = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
		SecretKey key = new SecretKeySpec(raw, "HmacSHA256");
		JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
		JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		TokenService service = new TokenService(encoder, "tienda-api", 30);

		var auth = UsernamePasswordAuthenticationToken.authenticated(
				"admin",
				null,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
		String token = service.crearToken(auth);
		Jwt decoded = decoder.decode(token);

		assertEquals("admin", decoded.getSubject());
		assertEquals("tienda-api", decoded.getClaimAsString("iss"));
		assertTrue(decoded.getClaimAsStringList("roles").contains("ROLE_ADMIN"));
		assertNotNull(decoded.getExpiresAt());
	}
}
