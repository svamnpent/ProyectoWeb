package com.phantom.api.service.impl;

import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.dto.request.ProductoRequestDTO;
import com.phantom.api.entity.Categoria;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.entity.Producto;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.CategoriaRepository;
import com.phantom.api.repository.EtiquetaRepository;
import com.phantom.api.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private EtiquetaRepository etiquetaRepository;

    @InjectMocks
    private ProductoServiceImpl service;

    private Categoria categoria;
    private Etiqueta etiqueta;
    private Producto producto;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("Electrónica").descripcion("Gadgets").build();
        etiqueta = Etiqueta.builder().id(1L).nombre("oferta").build();
        producto = Producto.builder()
                .id(10L).nombre("Laptop").descripcion("14 pulgadas")
                .precio(new BigDecimal("999.99")).stock(5).marca("Acme")
                .categoria(categoria).build();
        producto.getEtiquetas().add(etiqueta);
    }

    private ProductoRequestDTO.ProductoRequestDTOBuilder request() {
        return ProductoRequestDTO.builder()
                .nombre("  Laptop  ").descripcion("14 pulgadas")
                .precio(new BigDecimal("999.99")).stock(5).marca("Acme")
                .categoriaId(1L);
    }

    @Test
    @DisplayName("obtenerTodas - mapea productos con categoría y etiquetas")
    void obtenerTodas() {
        when(productoRepository.findAllConCategoriasYEtiquetas()).thenReturn(List.of(producto));

        List<ProductoDTO> resultado = service.obtenerTodas();

        assertEquals(1, resultado.size());
        assertEquals("Laptop", resultado.get(0).getNombre());
        assertEquals("Electrónica", resultado.get(0).getCategoria().getNombre());
        assertEquals(1, resultado.get(0).getEtiquetas().size());
    }

    @Test
    @DisplayName("obtenerPorId - existente")
    void obtenerPorId_existente() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        ProductoDTO dto = service.obtenerPorId(10L);

        assertEquals(10L, dto.getId());
        assertEquals(new BigDecimal("999.99"), dto.getPrecio());
    }

    @Test
    @DisplayName("obtenerPorId - inexistente lanza ResourceNotFoundException")
    void obtenerPorId_inexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.obtenerPorId(99L));
        assertEquals("Producto no encontrado con el ID: 99", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorPalabraClave - delega en la consulta de BD (no filtra en memoria)")
    void buscar_usaConsultaDeBD() {
        when(productoRepository.buscarConCategoriaYEtiquetas("lap")).thenReturn(List.of(producto));

        List<ProductoDTO> resultado = service.buscarPorPalabraClave("lap");

        assertEquals(1, resultado.size());
        verify(productoRepository).buscarConCategoriaYEtiquetas("lap");
        verify(productoRepository, never()).findAllConCategoriasYEtiquetas();
    }

    @Test
    @DisplayName("crear - guarda con etiquetas en un conjunto MUTABLE y nombre recortado")
    void crear_conEtiquetas() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(etiquetaRepository.findByIdIn(Set.of(1L))).thenReturn(List.of(etiqueta));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductoDTO dto = service.crear(request().etiquetaIds(Set.of(1L)).build());

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        Producto guardado = captor.getValue();
        assertEquals("Laptop", guardado.getNombre());
        assertDoesNotThrow(() -> guardado.getEtiquetas().add(Etiqueta.builder().id(2L).nombre("x").build()));
        assertEquals(10L, dto.getId());
    }

    @Test
    @DisplayName("crear - sin etiquetaIds no consulta etiquetas y el conjunto es mutable")
    void crear_sinEtiquetas() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(request().build());

        verifyNoInteractions(etiquetaRepository);
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertDoesNotThrow(() -> captor.getValue().getEtiquetas().add(etiqueta));
    }

    @Test
    @DisplayName("crear - categoría inexistente lanza ResourceNotFoundException")
    void crear_categoriaInexistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear(request().build()));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - etiqueta inexistente ya no se ignora en silencio")
    void crear_etiquetaInexistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(etiquetaRepository.findByIdIn(Set.of(1L, 2L))).thenReturn(List.of(etiqueta));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.crear(request().etiquetaIds(Set.of(1L, 2L)).build()));

        assertTrue(ex.getMessage().contains("2"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - cambia categoría y etiquetas")
    void actualizar_cambiaCategoriaYEtiquetas() {
        Categoria otra = Categoria.builder().id(2L).nombre("Hogar").build();
        Etiqueta nueva = Etiqueta.builder().id(5L).nombre("nuevo").build();
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(otra));
        when(etiquetaRepository.findByIdIn(Set.of(5L))).thenReturn(List.of(nueva));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO dto = service.actualizar(10L,
                request().categoriaId(2L).etiquetaIds(Set.of(5L)).stock(9).build());

        assertEquals("Hogar", dto.getCategoria().getNombre());
        assertEquals(9, dto.getStock());
        assertEquals(1, dto.getEtiquetas().size());
        assertEquals("nuevo", dto.getEtiquetas().iterator().next().getNombre());
    }

    @Test
    @DisplayName("actualizar - etiquetaIds null conserva las etiquetas actuales")
    void actualizar_etiquetaIdsNullConservaEtiquetas() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO dto = service.actualizar(10L, request().build());

        assertEquals(1, dto.getEtiquetas().size());
        verifyNoInteractions(etiquetaRepository);
        verify(categoriaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("actualizar - inexistente lanza ResourceNotFoundException")
    void actualizar_inexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.actualizar(99L, request().build()));
    }

    @Test
    @DisplayName("eliminar - existente")
    void eliminar_existente() {
        when(productoRepository.existsById(10L)).thenReturn(true);

        service.eliminar(10L);

        verify(productoRepository).deleteById(10L);
    }

    @Test
    @DisplayName("eliminar - inexistente lanza ResourceNotFoundException")
    void eliminar_inexistente() {
        when(productoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(99L));
        verify(productoRepository, never()).deleteById(any());
    }
}
