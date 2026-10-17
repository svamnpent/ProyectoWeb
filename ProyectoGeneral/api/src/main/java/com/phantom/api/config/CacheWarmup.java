package com.phantom.api.config;

import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.service.CategoriaService;
import com.phantom.api.service.EtiquetaService;
import com.phantom.api.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Precarga la caché local al arrancar con el catálogo (que es público y de lectura frecuente),
 * para que las primeras peticiones ya salgan de memoria. Se puede desactivar con
 * app.cache.warmup.enabled=false. Si falla, la aplicación arranca igual (solo se avisa).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.cache.warmup.enabled", havingValue = "true", matchIfMissing = true)
public class CacheWarmup implements ApplicationRunner, Ordered {

    /** Límite de productos precargados por ID (el listado completo siempre se precarga). */
    static final int MAX_PRODUCTOS_POR_ID = 200;

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final EtiquetaService etiquetaService;

    public CacheWarmup(ProductoService productoService, CategoriaService categoriaService,
                       EtiquetaService etiquetaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.etiquetaService = etiquetaService;
    }

    /** Después de DemoDataSeeder (10), para precargar también los datos demo. */
    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int categorias = categoriaService.obtenerTodas().size();
            int etiquetas = etiquetaService.obtenerTodas().size();
            List<ProductoDTO> productos = productoService.obtenerTodas();
            productos.stream().limit(MAX_PRODUCTOS_POR_ID)
                    .forEach(p -> productoService.obtenerPorId(p.getId()));
            log.info("Caché precargada: {} categorías, {} etiquetas, {} productos",
                    categorias, etiquetas, productos.size());
        } catch (RuntimeException e) {
            log.warn("No se pudo precargar la caché (la aplicación continúa): {}", e.getMessage());
        }
    }
}
