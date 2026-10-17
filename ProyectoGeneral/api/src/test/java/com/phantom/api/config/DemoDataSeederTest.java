package com.phantom.api.config;

import com.phantom.api.entity.*;
import com.phantom.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock private CategoriaRepository categoriaRepository;
    @Mock private EtiquetaRepository etiquetaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PerfilRepository perfilRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private DemoDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DemoDataSeeder(categoriaRepository, etiquetaRepository, productoRepository,
                usuarioRepository, perfilRepository, ventaRepository, passwordEncoder);
    }

    @SuppressWarnings("unchecked")
    private static <T> ArgumentCaptor<List<T>> listCaptor() {
        return ArgumentCaptor.forClass((Class<List<T>>) (Class<?>) List.class);
    }

    @Test
    @DisplayName("si ya hay categorías no carga nada (idempotente)")
    void noRecarga() {
        when(categoriaRepository.count()).thenReturn(3L);

        seeder.run(null);

        verifyNoInteractions(etiquetaRepository, productoRepository, usuarioRepository,
                perfilRepository, ventaRepository, passwordEncoder);
        verify(categoriaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("con la BD vacía carga catálogo, usuarios con password hasheado, perfiles y ventas")
    void cargaDatos() {
        when(categoriaRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(DemoDataSeeder.DEMO_PASSWORD)).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        ArgumentCaptor<List<Categoria>> categorias = listCaptor();
        ArgumentCaptor<List<Etiqueta>> etiquetas = listCaptor();
        ArgumentCaptor<List<Producto>> productos = listCaptor();
        ArgumentCaptor<List<Venta>> ventas = listCaptor();
        verify(categoriaRepository).saveAll(categorias.capture());
        verify(etiquetaRepository).saveAll(etiquetas.capture());
        verify(productoRepository).saveAll(productos.capture());
        verify(ventaRepository).saveAll(ventas.capture());
        verify(perfilRepository).saveAll(any());

        assertEquals(5, categorias.getValue().size());
        assertEquals(6, etiquetas.getValue().size());
        assertTrue(productos.getValue().size() >= 15);
        assertEquals(5, ventas.getValue().size());

        // Usuarios: 3 compradores + 1 trabajador, todos con el password hasheado (nunca en claro)
        ArgumentCaptor<Usuario> usuarios = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(4)).save(usuarios.capture());
        usuarios.getAllValues().forEach(u -> {
            assertEquals("HASH", u.getPassword());
            assertTrue(u.getEmail().endsWith("@demo.local"));
        });
        assertEquals(3, usuarios.getAllValues().stream().filter(u -> "USER".equals(u.getRol())).count());
        assertEquals(1, usuarios.getAllValues().stream().filter(u -> "TRABAJADOR".equals(u.getRol())).count());
        verify(passwordEncoder, times(1)).encode(any());
    }

    @Test
    @DisplayName("los productos son coherentes: precios positivos, stock nunca negativo, hay uno agotado")
    void productosCoherentes() {
        when(categoriaRepository.count()).thenReturn(0L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        ArgumentCaptor<List<Producto>> productos = listCaptor();
        verify(productoRepository).saveAll(productos.capture());
        List<Producto> lista = productos.getValue();
        lista.forEach(p -> {
            assertTrue(p.getPrecio().signum() > 0, p.getNombre());
            assertTrue(p.getStock() >= 0, "stock negativo en " + p.getNombre());
            assertNotNull(p.getCategoria(), p.getNombre());
            assertTrue(p.getNombre().length() <= 150);
        });
        assertTrue(lista.stream().anyMatch(p -> p.getStock() == 0), "falta un producto agotado para probar");
        Set<String> nombres = lista.stream().map(Producto::getNombre).collect(Collectors.toSet());
        assertEquals(lista.size(), nombres.size(), "nombres de producto repetidos");
    }

    @Test
    @DisplayName("las ventas son coherentes: total = suma de subtotales, items = suma de cantidades, orden única")
    void ventasCoherentes() {
        when(categoriaRepository.count()).thenReturn(0L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        ArgumentCaptor<List<Venta>> ventas = listCaptor();
        verify(ventaRepository).saveAll(ventas.capture());
        for (Venta v : ventas.getValue()) {
            BigDecimal suma = v.getDetalles().stream().map(DetalleVenta::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int items = v.getDetalles().stream().mapToInt(DetalleVenta::getCantidad).sum();
            assertEquals(0, suma.compareTo(v.getTotal()), v.getNumeroOrden());
            assertEquals(items, v.getCantidadItems(), v.getNumeroOrden());
            assertNotNull(v.getUsuario());
            assertTrue(v.getFecha().isBefore(java.time.Instant.now()));
            v.getDetalles().forEach(d -> assertEquals(0,
                    d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())).compareTo(d.getSubtotal())));
        }
        Set<String> ordenes = ventas.getValue().stream().map(Venta::getNumeroOrden).collect(Collectors.toSet());
        assertEquals(ventas.getValue().size(), ordenes.size());
    }

    @Test
    @DisplayName("las ventas descuentan stock: el stock final es el inicial menos lo vendido")
    void ventasDescuentanStock() {
        when(categoriaRepository.count()).thenReturn(0L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        ArgumentCaptor<List<Producto>> productos = listCaptor();
        verify(productoRepository).saveAll(productos.capture());
        Producto ideapad = productos.getValue().stream()
                .filter(p -> p.getNombre().contains("IdeaPad")).findFirst().orElseThrow();
        assertEquals(11, ideapad.getStock()); // 12 iniciales - 1 vendido en DEMO-0001
    }

    @Test
    @DisplayName("si un usuario demo ya existe se reutiliza en vez de duplicarlo")
    void reutilizaUsuarioExistente() {
        Usuario existente = Usuario.builder().id(99L).nombre("María").email("maria.quispe@demo.local")
                .rol("USER").build();
        when(categoriaRepository.count()).thenReturn(0L);
        when(usuarioRepository.findByEmailIgnoreCase(any())).thenAnswer(inv ->
                "maria.quispe@demo.local".equals(inv.getArgument(0))
                        ? java.util.Optional.of(existente) : java.util.Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        verify(usuarioRepository, times(3)).save(any(Usuario.class)); // Carlos, Lucía y el trabajador
    }

    @Test
    @DisplayName("se ejecuta antes que la precarga de caché")
    void ordenAnteriorAlWarmup() {
        assertEquals(10, seeder.getOrder());
    }
}
