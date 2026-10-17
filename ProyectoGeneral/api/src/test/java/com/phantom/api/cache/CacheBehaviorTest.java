package com.phantom.api.cache;

import com.phantom.api.config.CacheConfig;
import com.phantom.api.config.CacheNames;
import com.phantom.api.dto.request.CategoriaRequestDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.dto.request.EtiquetaRequestDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.entity.Categoria;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.entity.Producto;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.*;
import com.phantom.api.security.CurrentUser;
import com.phantom.api.service.CategoriaService;
import com.phantom.api.service.EtiquetaService;
import com.phantom.api.service.ProductoService;
import com.phantom.api.service.VentaService;
import com.phantom.api.service.impl.CategoriaServiceImpl;
import com.phantom.api.service.impl.EtiquetaServiceImpl;
import com.phantom.api.service.impl.ProductoServiceImpl;
import com.phantom.api.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas LOCALES de la caché (Caffeine real, repositorios simulados, sin BD ni Docker):
 * verifican que las lecturas repetidas no vuelven a la BD y que cada escritura invalida
 * las cachés correctas.
 */
@SpringBootTest(classes = CacheBehaviorTest.TestConfig.class)
class CacheBehaviorTest {

    @Configuration
    @Import({CacheConfig.class, CategoriaServiceImpl.class, EtiquetaServiceImpl.class,
            ProductoServiceImpl.class, VentaServiceImpl.class})
    @ImportAutoConfiguration(CacheAutoConfiguration.class)
    static class TestConfig {
    }

    @Autowired private CacheManager cacheManager;
    @Autowired private CategoriaService categoriaService;
    @Autowired private EtiquetaService etiquetaService;
    @Autowired private ProductoService productoService;
    @Autowired private VentaService ventaService;

    @MockBean private CategoriaRepository categoriaRepository;
    @MockBean private EtiquetaRepository etiquetaRepository;
    @MockBean private ProductoRepository productoRepository;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private VentaRepository ventaRepository;
    @MockBean private CurrentUser currentUser;

    private Categoria categoria;
    private Etiqueta etiqueta;
    private Producto producto;

    @BeforeEach
    void setUp() {
        // Los contextos de Spring se reutilizan entre tests: hay que vaciar las cachés
        cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());

        categoria = Categoria.builder().id(1L).nombre("Electrónica").build();
        etiqueta = Etiqueta.builder().id(1L).nombre("oferta").build();
        producto = Producto.builder().id(10L).nombre("Phone").precio(new BigDecimal("100.00"))
                .stock(5).categoria(categoria).build();
    }

    @Test
    @DisplayName("el CacheManager es Caffeine y registra estadísticas")
    void cacheManagerEsCaffeine() {
        assertInstanceOf(CaffeineCacheManager.class, cacheManager);

        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        categoriaService.obtenerTodas();
        categoriaService.obtenerTodas();

        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(CacheNames.CATEGORIAS);
        assertEquals(1, cache.getNativeCache().stats().hitCount());
        assertEquals(1, cache.getNativeCache().stats().missCount());
    }

    // ------------------------------------------------------------ categorías

    @Test
    @DisplayName("categorías: el listado repetido se sirve desde caché (1 sola consulta)")
    void categorias_listadoCacheado() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        categoriaService.obtenerTodas();
        categoriaService.obtenerTodas();
        categoriaService.obtenerTodas();

        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("categorías: cada ID se cachea por separado")
    void categorias_porIdSeparados() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(
                Categoria.builder().id(2L).nombre("Hogar").build()));

        assertEquals("Electrónica", categoriaService.obtenerPorId(1L).getNombre());
        assertEquals("Hogar", categoriaService.obtenerPorId(2L).getNombre());
        categoriaService.obtenerPorId(1L);
        categoriaService.obtenerPorId(2L);

        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("categorías: crear invalida el listado cacheado")
    void categorias_crearInvalida() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Hogar")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        categoriaService.obtenerTodas();
        categoriaService.crear(CategoriaRequestDTO.builder().nombre("Hogar").build());
        categoriaService.obtenerTodas();

        verify(categoriaRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("categorías: actualizar invalida categorías Y productos (el producto embebe la categoría)")
    void categorias_actualizarInvalidaProductos() {
        when(productoRepository.findAllConCategoriasYEtiquetas()).thenReturn(List.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Gadgets")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        productoService.obtenerTodas();
        productoService.obtenerTodas();
        verify(productoRepository, times(1)).findAllConCategoriasYEtiquetas();

        categoriaService.actualizar(1L, CategoriaRequestDTO.builder().nombre("Gadgets").build());
        productoService.obtenerTodas();

        verify(productoRepository, times(2)).findAllConCategoriasYEtiquetas();
    }

    @Test
    @DisplayName("categorías: eliminar invalida categorías y productos")
    void categorias_eliminarInvalida() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        when(productoRepository.findAllConCategoriasYEtiquetas()).thenReturn(List.of(producto));
        when(categoriaRepository.existsById(1L)).thenReturn(true);

        categoriaService.obtenerTodas();
        productoService.obtenerTodas();
        categoriaService.eliminar(1L);
        categoriaService.obtenerTodas();
        productoService.obtenerTodas();

        verify(categoriaRepository, times(2)).findAll();
        verify(productoRepository, times(2)).findAllConCategoriasYEtiquetas();
    }

    @Test
    @DisplayName("categorías: la búsqueda no distingue mayúsculas en la clave de caché")
    void categorias_busquedaNormalizada() {
        when(categoriaRepository.findByNombreContainingIgnoreCase(any())).thenReturn(List.of(categoria));

        categoriaService.buscarPorNombre("Elec");
        categoriaService.buscarPorNombre("elec");

        verify(categoriaRepository, times(1)).findByNombreContainingIgnoreCase(any());
    }

    // ------------------------------------------------------------ etiquetas

    @Test
    @DisplayName("etiquetas: lectura cacheada; crear invalida etiquetas; eliminar invalida también productos")
    void etiquetas_cacheEInvalidaciones() {
        when(etiquetaRepository.findAll()).thenReturn(List.of(etiqueta));
        when(productoRepository.findAllConCategoriasYEtiquetas()).thenReturn(List.of(producto));
        when(etiquetaRepository.existsByNombreIgnoreCase("nueva")).thenReturn(false);
        when(etiquetaRepository.save(any(Etiqueta.class))).thenReturn(etiqueta);
        when(etiquetaRepository.existsById(1L)).thenReturn(true);

        etiquetaService.obtenerTodas();
        etiquetaService.obtenerTodas();
        verify(etiquetaRepository, times(1)).findAll();

        etiquetaService.crear(EtiquetaRequestDTO.builder().nombre("nueva").build());
        etiquetaService.obtenerTodas();
        verify(etiquetaRepository, times(2)).findAll();

        productoService.obtenerTodas();
        etiquetaService.eliminar(1L);
        productoService.obtenerTodas();
        verify(productoRepository, times(2)).findAllConCategoriasYEtiquetas();
    }

    // ------------------------------------------------------------ productos

    @Test
    @DisplayName("productos: obtenerPorId cacheado")
    void productos_porIdCacheado() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        productoService.obtenerPorId(10L);
        productoService.obtenerPorId(10L);

        verify(productoRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("productos: la búsqueda se cachea por palabra clave sin distinguir mayúsculas")
    void productos_busquedaCacheada() {
        when(productoRepository.buscarConCategoriaYEtiquetas(any())).thenReturn(List.of(producto));

        productoService.buscarPorPalabraClave("Phone");
        productoService.buscarPorPalabraClave("phone");
        productoService.buscarPorPalabraClave("tablet");

        verify(productoRepository, times(2)).buscarConCategoriaYEtiquetas(any());
    }

    @Test
    @DisplayName("productos: las excepciones NO se cachean")
    void productos_erroresNoSeCachean() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productoService.obtenerPorId(99L));
        assertThrows(ResourceNotFoundException.class, () -> productoService.obtenerPorId(99L));

        verify(productoRepository, times(2)).findById(99L);
    }

    @Test
    @DisplayName("productos: eliminar invalida la caché")
    void productos_eliminarInvalida() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsById(10L)).thenReturn(true);

        productoService.obtenerPorId(10L);
        productoService.eliminar(10L);
        productoService.obtenerPorId(10L);

        verify(productoRepository, times(2)).findById(10L);
    }

    // ------------------------------------------------------------ ventas -> stock

    @Test
    @DisplayName("ventas: una compra invalida la caché de productos (el stock cambió)")
    void ventas_crearInvalidaProductos() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(currentUser.isStaff()).thenReturn(true);
        when(productoRepository.findAllById(any())).thenReturn(List.of(producto));
        when(productoRepository.descontarStock(10L, 2)).thenReturn(1);
        when(ventaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(5, productoService.obtenerPorId(10L).getStock());
        productoService.obtenerPorId(10L);
        verify(productoRepository, times(1)).findById(10L);

        ventaService.crear(VentaRequestDTO.builder()
                .detalles(List.of(DetalleVentaRequestDTO.builder().productoId(10L).cantidad(2).build()))
                .build());
        productoService.obtenerPorId(10L);

        verify(productoRepository, times(2)).findById(10L);
    }

    @Test
    @DisplayName("ventas: una compra rechazada por falta de stock NO invalida la caché")
    void ventas_compraFallidaNoInvalida() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(currentUser.isStaff()).thenReturn(true);
        when(productoRepository.findAllById(any())).thenReturn(List.of(producto));
        when(productoRepository.descontarStock(10L, 9)).thenReturn(0);

        productoService.obtenerPorId(10L);
        assertThrows(RuntimeException.class, () -> ventaService.crear(VentaRequestDTO.builder()
                .detalles(List.of(DetalleVentaRequestDTO.builder().productoId(10L).cantidad(9).build()))
                .build()));
        productoService.obtenerPorId(10L);

        verify(productoRepository, times(1)).findById(10L);
    }
}
