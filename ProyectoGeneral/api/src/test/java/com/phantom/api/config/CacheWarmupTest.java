package com.phantom.api.config;

import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.service.CategoriaService;
import com.phantom.api.service.EtiquetaService;
import com.phantom.api.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheWarmupTest {

    @Mock
    private ProductoService productoService;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private EtiquetaService etiquetaService;

    private CacheWarmup warmup() {
        return new CacheWarmup(productoService, categoriaService, etiquetaService);
    }

    @Test
    @DisplayName("precarga categorías, etiquetas, el listado de productos y cada producto por ID")
    void precargaTodo() {
        when(categoriaService.obtenerTodas()).thenReturn(List.of(CategoriaDTO.builder().id(1L).build()));
        when(etiquetaService.obtenerTodas()).thenReturn(List.of(EtiquetaDTO.builder().id(1L).build()));
        when(productoService.obtenerTodas()).thenReturn(List.of(
                ProductoDTO.builder().id(10L).build(), ProductoDTO.builder().id(11L).build()));

        warmup().run(null);

        verify(categoriaService).obtenerTodas();
        verify(etiquetaService).obtenerTodas();
        verify(productoService).obtenerTodas();
        verify(productoService).obtenerPorId(10L);
        verify(productoService).obtenerPorId(11L);
    }

    @Test
    @DisplayName("limita la precarga por ID para no llenar la caché con catálogos enormes")
    void respetaElLimite() {
        List<ProductoDTO> muchos = new ArrayList<>();
        for (long i = 1; i <= CacheWarmup.MAX_PRODUCTOS_POR_ID + 50; i++) {
            muchos.add(ProductoDTO.builder().id(i).build());
        }
        when(productoService.obtenerTodas()).thenReturn(muchos);

        warmup().run(null);

        verify(productoService, times(CacheWarmup.MAX_PRODUCTOS_POR_ID)).obtenerPorId(anyLong());
    }

    @Test
    @DisplayName("si la precarga falla, la aplicación arranca igual")
    void toleraFallos() {
        when(categoriaService.obtenerTodas()).thenThrow(new RuntimeException("BD caída"));

        assertDoesNotThrow(() -> warmup().run(null));
    }

    @Test
    @DisplayName("se ejecuta después del seeder de datos demo")
    void ordenPosteriorAlSeeder() {
        org.junit.jupiter.api.Assertions.assertTrue(warmup().getOrder() > 10);
    }
}
