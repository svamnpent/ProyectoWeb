package com.phantom.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/** El orden del interceptor permite invalidar la caché después del commit. */
@Configuration
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 10)
public class CacheConfig {
}
