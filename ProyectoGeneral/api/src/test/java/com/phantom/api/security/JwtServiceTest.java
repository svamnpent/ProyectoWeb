package com.phantom.api.security;

import com.phantom.api.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres-123456";
    private static final String EMISOR = "phantom-test";

    private final JwtConfig config = new JwtConfig();
    private final Usuario usuario = Usuario.builder().id(42L).nombre("Ana").email("ana@x.com").rol("ADMIN").build();

    private JwtService servicio(String secreto, long minutos, String emisor) {
        JwtProperties props = new JwtProperties(secreto, minutos, emisor);
        JwtEncoder encoder = config.jwtEncoder(props);
        return new JwtService(encoder, props);
    }

    private JwtDecoder decoder(String secreto, String emisor) {
        return config.jwtDecoder(new JwtProperties(secreto, 60, emisor));
    }

    @Test
    @DisplayName("el token contiene sub (id), email, roles, emisor y expiración configurada")
    void claimsCorrectos() {
        String token = servicio(SECRETO, 60, EMISOR).generarToken(usuario);

        Jwt jwt = decoder(SECRETO, EMISOR).decode(token);

        assertEquals("42", jwt.getSubject());
        assertEquals("ana@x.com", jwt.getClaimAsString(JwtService.CLAIM_EMAIL));
        assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList(JwtService.CLAIM_ROLES));
        assertEquals(EMISOR, jwt.getClaimAsString("iss"));
        assertEquals(Duration.ofMinutes(60), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertNotNull(jwt.getId());
    }

    @Test
    @DisplayName("getExpiracionSegundos convierte minutos a segundos")
    void expiracionEnSegundos() {
        assertEquals(1800L, servicio(SECRETO, 30, EMISOR).getExpiracionSegundos());
    }

    @Test
    @DisplayName("un token firmado con otra clave es rechazado")
    void otraClave() {
        String token = servicio("otra-clave-distinta-con-mas-de-32-caracteres-999999", 60, EMISOR)
                .generarToken(usuario);

        assertThrows(JwtException.class, () -> decoder(SECRETO, EMISOR).decode(token));
    }

    @Test
    @DisplayName("un token con el payload alterado es rechazado (firma inválida)")
    void payloadAlterado() {
        String token = servicio(SECRETO, 60, EMISOR).generarToken(usuario);
        String[] partes = token.split("\\.");
        String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"1\",\"iss\":\"" + EMISOR + "\",\"roles\":[\"ADMIN\"]}")
                        .getBytes(StandardCharsets.UTF_8));
        String alterado = partes[0] + "." + payloadFalso + "." + partes[2];

        assertThrows(JwtException.class, () -> decoder(SECRETO, EMISOR).decode(alterado));
    }

    @Test
    @DisplayName("un token expirado es rechazado")
    void expirado() {
        String token = servicio(SECRETO, -5, EMISOR).generarToken(usuario);

        assertThrows(JwtException.class, () -> decoder(SECRETO, EMISOR).decode(token));
    }

    @Test
    @DisplayName("un token de otro emisor es rechazado")
    void otroEmisor() {
        String token = servicio(SECRETO, 60, "otro-emisor").generarToken(usuario);

        assertThrows(JwtException.class, () -> decoder(SECRETO, EMISOR).decode(token));
    }

    @Test
    @DisplayName("un secreto de menos de 32 caracteres impide arrancar")
    void secretoCorto() {
        JwtProperties props = new JwtProperties("corto", 60, EMISOR);

        assertThrows(IllegalStateException.class, () -> config.jwtEncoder(props));
        assertThrows(IllegalStateException.class, () -> config.jwtDecoder(props));
    }
}
