package com.phantom.api.service.impl;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.entity.DetalleVenta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Usuario;
import com.phantom.api.entity.Venta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.StockInsuficienteException;
import com.phantom.api.repository.DetalleVentaRepository;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.VentaRepository;
import com.phantom.api.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetalleVentaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private DetalleVentaServiceImpl service;

    private Producto producto;
    private Venta venta;

    @BeforeEach
    void setUp() {
        producto = Producto.builder().id(1L).nombre("A").precio(new BigDecimal("10.00")).stock(5).build();
        Usuario dueno = Usuario.builder().id(7L).nombre("Ana").email("ana@x.com").build();
        venta = Venta.builder().id(100L).total(new BigDecimal("20.00")).cantidadItems(2).usuario(dueno).build();
    }

    @Test
    @DisplayName("obtenerPorVentaId - valida dueño o ADMIN y lista los detalles")
    void obtenerPorVentaId_ok() {
        DetalleVenta detalle = DetalleVenta.builder().id(1L).venta(venta).producto(producto).cantidad(2)
                .precioUnitario(new BigDecimal("10.00")).subtotal(new BigDecimal("20.00")).build();
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        when(detalleVentaRepository.findByVentaId(100L)).thenReturn(List.of(detalle));

        List<DetalleVentaDTO> resultado = service.obtenerPorVentaId(100L);

        assertEquals(1, resultado.size());
        assertEquals("A", resultado.get(0).getProductoNombre());
        verify(currentUser).requireSelfOrAdmin(7L);
    }

    @Test
    @DisplayName("obtenerPorVentaId - venta ajena rechazada sin listar detalles")
    void obtenerPorVentaId_ajena() {
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        doThrow(new AccessDeniedException("no")).when(currentUser).requireSelfOrAdmin(7L);

        assertThrows(AccessDeniedException.class, () -> service.obtenerPorVentaId(100L));
        verifyNoInteractions(detalleVentaRepository);
    }

    @Test
    @DisplayName("obtenerPorVentaId - venta anónima solo para ADMIN")
    void obtenerPorVentaId_anonima() {
        venta.setUsuario(null);
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        when(currentUser.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.obtenerPorVentaId(100L));
    }

    @Test
    @DisplayName("obtenerPorVentaId - venta inexistente")
    void obtenerPorVentaId_inexistente() {
        when(ventaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorVentaId(9L));
    }

    @Test
    @DisplayName("crear - descuenta stock y actualiza total e items de la venta")
    void crear_ok() {
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.descontarStock(1L, 3)).thenReturn(1);
        when(detalleVentaRepository.save(any(DetalleVenta.class))).thenAnswer(inv -> {
            DetalleVenta d = inv.getArgument(0);
            d.setId(50L);
            return d;
        });

        DetalleVentaDTO dto = service.crear(100L,
                DetalleVentaRequestDTO.builder().productoId(1L).cantidad(3).build());

        assertEquals(0, new BigDecimal("30.00").compareTo(dto.getSubtotal()));
        assertEquals(0, new BigDecimal("50.00").compareTo(venta.getTotal()));
        assertEquals(5, venta.getCantidadItems());
    }

    @Test
    @DisplayName("crear - sin stock lanza StockInsuficienteException y no toca la venta")
    void crear_sinStock() {
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.descontarStock(1L, 9)).thenReturn(0);

        assertThrows(StockInsuficienteException.class, () -> service.crear(100L,
                DetalleVentaRequestDTO.builder().productoId(1L).cantidad(9).build()));

        assertEquals(0, new BigDecimal("20.00").compareTo(venta.getTotal()));
        assertEquals(2, venta.getCantidadItems());
        verify(detalleVentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - venta o producto inexistente")
    void crear_inexistentes() {
        when(ventaRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.crear(9L,
                DetalleVentaRequestDTO.builder().productoId(1L).cantidad(1).build()));

        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        when(productoRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.crear(100L,
                DetalleVentaRequestDTO.builder().productoId(8L).cantidad(1).build()));
    }

    @Test
    @DisplayName("eliminar - repone stock y resta del total de la venta")
    void eliminar_ok() {
        venta.setTotal(new BigDecimal("45.50"));
        venta.setCantidadItems(3);
        DetalleVenta detalle = DetalleVenta.builder().id(1L).venta(venta).producto(producto).cantidad(2)
                .precioUnitario(new BigDecimal("10.00")).subtotal(new BigDecimal("20.00")).build();
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalle));

        service.eliminar(1L);

        verify(productoRepository).reponerStock(1L, 2);
        verify(detalleVentaRepository).delete(detalle);
        assertEquals(0, new BigDecimal("25.50").compareTo(venta.getTotal()));
        assertEquals(1, venta.getCantidadItems());
    }

    @Test
    @DisplayName("eliminar - inexistente")
    void eliminar_inexistente() {
        when(detalleVentaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(9L));
        verifyNoInteractions(productoRepository);
    }
}
