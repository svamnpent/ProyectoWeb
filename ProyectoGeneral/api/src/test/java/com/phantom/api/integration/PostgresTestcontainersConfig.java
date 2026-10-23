package com.phantom.api.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Levanta un PostgreSQL REAL en Docker para los tests de integración y conecta
 * automáticamente DataSource y Flyway a él (@ServiceConnection).
 * Usa la misma versión de imagen que docker-compose.yml.
 * El contenedor se comparte entre todos los tests *IT (un único contexto de Spring).
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestcontainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }
}
