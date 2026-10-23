package com.phantom.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Activa la caché. El tipo (Caffeine), el tamaño máximo y el TTL se configuran en
 * application.properties (spring.cache.*).
 *
 * El "order" hace que el interceptor de caché envuelva al transaccional: así los
 * {@code @CacheEvict} se ejecutan DESPUÉS del commit y un lector concurrente no puede
 * volver a cachear datos viejos entre la invalidación y el commit.
 */
@Configuration
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 10)
public class CacheConfig {
}
