package com.phantom.api.security;

import com.phantom.api.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_EMAIL = "email";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    /**
     * Genera un access token. El "sub" es el ID del usuario; los roles viajan en el claim "roles".
     */
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(properties.expirationMinutes(), ChronoUnit.MINUTES))
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .claim(CLAIM_ROLES, List.of(usuario.getRol()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiracionSegundos() {
        return properties.expirationMinutes() * 60;
    }
}
