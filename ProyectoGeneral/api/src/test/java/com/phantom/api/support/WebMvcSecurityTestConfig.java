package com.phantom.api.support;

import com.phantom.api.security.CurrentUser;
import com.phantom.api.security.RestAccessDeniedHandler;
import com.phantom.api.security.RestAuthenticationEntryPoint;
import com.phantom.api.security.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Carga la seguridad REAL de la aplicación dentro de los tests @WebMvcTest
 * (los slices no escanean @Configuration ni @Component).
 * El JwtDecoder rechaza cualquier token: los tests se autentican con JwtTestUtils.
 */
@TestConfiguration
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
        CurrentUser.class})
public class WebMvcSecurityTestConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            throw new BadJwtException("Token inválido (decoder de pruebas)");
        };
    }
}
