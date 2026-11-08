package com.utp.semana4_api_rest.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
        @Test
        void tokenContieneUsuarioYRolFirmados() {
                byte[] raw = "0123456789abcdef0123456789abcdef"
                                .getBytes(StandardCharsets.UTF_8);
                SecretKey key = new SecretKeySpec(raw, "HmacSHA256");
                JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
                JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                                .macAlgorithm(MacAlgorithm.HS256).build();
                TokenService service = new TokenService(encoder, "tienda-api", 30);
                var auth = UsernamePasswordAuthenticationToken.authenticated(
                                "admin", null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                String token = service.crearToken(auth);
                Jwt decoded = decoder.decode(token);
                assertEquals("admin", decoded.getSubject());
                assertEquals("tienda-api", decoded.getIssuer().toString());
                assertTrue(decoded.getClaimAsStringList("roles")
                                .contains("ROLE_ADMIN"));
                assertNotNull(decoded.getExpiresAt());
        }
}