package com.phantom.api.service.impl;

import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.entity.DetalleVenta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Usuario;
import com.phantom.api.entity.Venta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.StockInsuficienteException;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.repository.VentaRepository;
import com.phantom.api.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private VentaServiceImpl service;

    private Producto productoA;
    private Producto productoB;
    private Usuario comprador;

    @BeforeEach
    void setUp() {
        productoA = Producto.builder().id(1L).nombre("A").precio(new BigDecimal("10.00")).stock(5).build();
        productoB = Producto.builder().id(2L).nombre("B").precio(new BigDecimal("25.50")).stock(3).build();
        comprador = Usuario.builder().id(7L).nombre("Ana").email("ana@x.com").rol("USER").build();
    }

    private DetalleVentaRequestDTO linea(Long productoId, int cantidad) {
        return DetalleVentaRequestDTO.builder().productoId(productoId).cantidad(cantidad).build();
    }

    private void guardarAsignandoId() {
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(100L);
            return v;
        });
    }

    private Venta ventaExistente(Usuario dueno) {
        Venta venta = Venta.builder().id(100L).total(new BigDecimal("45.50")).cantidadItems(3)
                .numeroOrden("ABC123").usuario(dueno).build();
        venta.getDetalles().add(DetalleVenta.builder().id(1L).venta(venta).producto(productoA)
                .cantidad(2).precioUnitario(new BigDecimal("10.00")).subtotal(new BigDecimal("20.00")).build());
        venta.getDetalles().add(DetalleVenta.builder().id(2L).venta(venta).producto(productoB)
                .cantidad(1).precioUnitario(new BigDecimal("25.50")).subtotal(new BigDecimal("25.50")).build());
        return venta;
    }

    // ---------------------------------------------------------------- crear

    @Test
    @DisplayName("crear - usuario normal: compra a su nombre (ignora usuarioId), calcula totales y descuenta stock")
    void crear_usuarioNormal() {
        when(currentUser.isAdmin()).thenReturn(false);
        when(currentUser.getId()).thenReturn(7L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA, productoB));
        when(productoRepository.descontarStock(1L, 2)).thenReturn(1);
        when(productoRepository.descontarStock(2L, 1)).thenReturn(1);
        guardarAsignandoId();

        VentaRequestDTO request = VentaRequestDTO.builder()
                .usuarioId(99L) // un usuario normal NO puede comprar a nombre de otro
                .detalles(List.of(linea(1L, 2), linea(2L, 1)))
                .build();

        VentaDTO dto = service.crear(request);

        assertEquals(0, new BigDecimal("45.50").compareTo(dto.getTotal()));
        assertEquals(3, dto.getCantidadItems());
        assertEquals(2, dto.getDetalles().size());
        assertEquals(7L, dto.getUsuario().getId());
        assertEquals(12, dto.getNumeroOrden().length());
        verify(usuarioRepository, never()).findById(99L);
        verify(productoRepository).descontarStock(1L, 2);
        verify(productoRepository).descontarStock(2L, 1);
    }

    @Test
    @DisplayName("crear - el total y los items quedan seteados ANTES del save (la BD los exige NOT NULL)")
    void crear_totalesSeteadosAntesDelSave() {
        when(currentUser.isAdmin()).thenReturn(false);
        when(currentUser.getId()).thenReturn(7L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA));
        when(productoRepository.descontarStock(1L, 2)).thenReturn(1);
        guardarAsignandoId();

        service.crear(VentaRequestDTO.builder().detalles(List.of(linea(1L, 2))).build());

        ArgumentCaptor<Venta> captor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaRepository, times(1)).save(captor.capture());
        Venta guardada = captor.getValue();
        assertNotNull(guardada.getTotal());
        assertNotNull(guardada.getCantidadItems());
        assertEquals(1, guardada.getDetalles().size());
        assertSame(guardada, guardada.getDetalles().get(0).getVenta());
    }

    @Test
    @DisplayName("crear - une líneas repetidas del mismo producto en una sola")
    void crear_unificaLineasRepetidas() {
        when(currentUser.isAdmin()).thenReturn(false);
        when(currentUser.getId()).thenReturn(7L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA));
        when(productoRepository.descontarStock(1L, 3)).thenReturn(1);
        guardarAsignandoId();

        VentaDTO dto = service.crear(VentaRequestDTO.builder()
                .detalles(List.of(linea(1L, 1), linea(1L, 2))).build());

        assertEquals(1, dto.getDetalles().size());
        assertEquals(3, dto.getDetalles().get(0).getCantidad());
        assertEquals(0, new BigDecimal("30.00").compareTo(dto.getTotal()));
        verify(productoRepository, times(1)).descontarStock(1L, 3);
    }

    @Test
    @DisplayName("crear - ADMIN puede registrar una venta sin usuario")
    void crear_adminSinUsuario() {
        when(currentUser.isAdmin()).thenReturn(true);
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA));
        when(productoRepository.descontarStock(1L, 1)).thenReturn(1);
        guardarAsignandoId();

        VentaDTO dto = service.crear(VentaRequestDTO.builder().detalles(List.of(linea(1L, 1))).build());

        assertNull(dto.getUsuario());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("crear - ADMIN puede registrar una venta para otro usuario")
    void crear_adminParaOtroUsuario() {
        when(currentUser.isAdmin()).thenReturn(true);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(
                Usuario.builder().id(3L).nombre("Luis").email("l@x.com").rol("USER").build()));
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA));
        when(productoRepository.descontarStock(1L, 1)).thenReturn(1);
        guardarAsignandoId();

        VentaDTO dto = service.crear(VentaRequestDTO.builder()
                .usuarioId(3L).detalles(List.of(linea(1L, 1))).build());

        assertEquals(3L, dto.getUsuario().getId());
    }

    @Test
    @DisplayName("crear - producto inexistente: no descuenta stock ni guarda")
    void crear_productoInexistente() {
        when(currentUser.isAdmin()).thenReturn(true);
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.crear(VentaRequestDTO.builder()
                        .detalles(List.of(linea(1L, 1), linea(2L, 1))).build()));

        assertTrue(ex.getMessage().contains("2"));
        verify(productoRepository, never()).descontarStock(anyLong(), anyInt());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - stock insuficiente: informa TODOS los productos con problema y no guarda")
    void crear_stockInsuficiente() {
        when(currentUser.isAdmin()).thenReturn(true);
        when(productoRepository.findAllById(any())).thenReturn(List.of(productoA, productoB));
        when(productoRepository.descontarStock(1L, 9)).thenReturn(0);
        when(productoRepository.descontarStock(2L, 9)).thenReturn(0);

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> service.crear(VentaRequestDTO.builder()
                        .detalles(List.of(linea(1L, 9), linea(2L, 9))).build()));

        assertEquals(2, ex.getErrores().size());
        assertTrue(ex.getErrores().get(0).contains("'A'"));
        assertTrue(ex.getErrores().get(1).contains("'B'"));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - usuario inexistente")
    void crear_usuarioInexistente() {
        when(currentUser.isAdmin()).thenReturn(false);
        when(currentUser.getId()).thenReturn(7L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.crear(VentaRequestDTO.builder().detalles(List.of(linea(1L, 1))).build()));
        verifyNoInteractions(productoRepository);
    }

    // ---------------------------------------------------------------- lecturas

    @Test
    @DisplayName("obtenerPorId - valida dueño o ADMIN")
    void obtenerPorId_ok() {
        when(ventaRepository.findByIdConDetallesYProducto(100L)).thenReturn(Optional.of(ventaExistente(comprador)));

        VentaDTO dto = service.obtenerPorId(100L);

        assertEquals(100L, dto.getId());
        assertEquals(2, dto.getDetalles().size());
        verify(currentUser).requireSelfOrAdmin(7L);
    }

    @Test
    @DisplayName("obtenerPorId - venta de otro usuario es rechazada")
    void obtenerPorId_ajena() {
        when(ventaRepository.findByIdConDetallesYProducto(100L)).thenReturn(Optional.of(ventaExistente(comprador)));
        doThrow(new AccessDeniedException("no")).when(currentUser).requireSelfOrAdmin(7L);

        assertThrows(AccessDeniedException.class, () -> service.obtenerPorId(100L));
    }

    @Test
    @DisplayName("obtenerPorId - venta sin usuario: solo ADMIN")
    void obtenerPorId_anonima() {
        when(ventaRepository.findByIdConDetallesYProducto(100L)).thenReturn(Optional.of(ventaExistente(null)));
        when(currentUser.isAdmin()).thenReturn(false, true);

        assertThrows(AccessDeniedException.class, () -> service.obtenerPorId(100L));
        assertDoesNotThrow(() -> service.obtenerPorId(100L));
    }

    @Test
    @DisplayName("obtenerPorId - inexistente")
    void obtenerPorId_inexistente() {
        when(ventaRepository.findByIdConDetallesYProducto(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(9L));
    }

    @Test
    @DisplayName("obtenerMisVentas - solo las del usuario autenticado")
    void obtenerMisVentas() {
        when(currentUser.getId()).thenReturn(7L);
        when(ventaRepository.findByUsuarioIdOrderByFechaDesc(7L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(ventaExistente(comprador))));

        List<VentaDTO> ventas = service.obtenerMisVentas();

        assertEquals(1, ventas.size());
        assertEquals(7L, ventas.get(0).getUsuario().getId());
    }

    @Test
    @DisplayName("obtenerTodas")
    void obtenerTodas() {
        when(ventaRepository.findAll()).thenReturn(List.of(ventaExistente(comprador)));

        assertEquals(1, service.obtenerTodas().size());
    }

    // ---------------------------------------------------------------- eliminar

    @Test
    @DisplayName("eliminar - repone el stock de cada producto y borra la venta")
    void eliminar_reponeStock() {
        Venta venta = ventaExistente(comprador);
        when(ventaRepository.findByIdConDetallesYProducto(100L)).thenReturn(Optional.of(venta));

        service.eliminar(100L);

        verify(productoRepository).reponerStock(1L, 2);
        verify(productoRepository).reponerStock(2L, 1);
        verify(ventaRepository).delete(venta);
    }

    @Test
    @DisplayName("eliminar - inexistente no repone nada")
    void eliminar_inexistente() {
        when(ventaRepository.findByIdConDetallesYProducto(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(9L));
        verify(productoRepository, never()).reponerStock(anyLong(), eq(1));
        verify(ventaRepository, never()).delete(any());
    }
}
