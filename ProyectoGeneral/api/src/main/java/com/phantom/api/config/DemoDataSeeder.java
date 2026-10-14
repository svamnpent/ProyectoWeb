package com.phantom.api.config;

import com.phantom.api.entity.*;
import com.phantom.api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Datos FALSOS de demostración (categorías, etiquetas, productos, usuarios, perfiles y ventas).
 * Solo existe con app.seed.enabled=true (perfil "demo"). NUNCA lo actives en producción:
 * crea usuarios con una contraseña conocida.
 *
 * Es idempotente: si ya hay categorías no hace nada.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner, Ordered {

    public static final String DEMO_PASSWORD = "Demo12345";

    private final CategoriaRepository categoriaRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final VentaRepository ventaRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(CategoriaRepository categoriaRepository,
                          EtiquetaRepository etiquetaRepository,
                          ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          VentaRepository ventaRepository,
                          PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.etiquetaRepository = etiquetaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.ventaRepository = ventaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Se ejecuta antes que el precalentamiento de caché (que usa estos datos). */
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoriaRepository.count() > 0) {
            log.info("Datos demo: ya existen categorías, no se carga nada");
            return;
        }

        // ---- Categorías
        Categoria laptops = categoria("Laptops", "Laptops y notebooks para trabajo, estudio y diseño");
        Categoria smartphones = categoria("Smartphones", "Teléfonos inteligentes de todas las gamas");
        Categoria audio = categoria("Audio", "Audífonos, parlantes y accesorios de sonido");
        Categoria gaming = categoria("Gaming", "Periféricos y accesorios para jugadores");
        Categoria accesorios = categoria("Accesorios", "Cargadores, cables, mochilas y más");
        categoriaRepository.saveAll(List.of(laptops, smartphones, audio, gaming, accesorios));

        // ---- Etiquetas
        Etiqueta oferta = etiqueta("oferta");
        Etiqueta nuevo = etiqueta("nuevo");
        Etiqueta premium = etiqueta("premium");
        Etiqueta economico = etiqueta("económico");
        Etiqueta bluetooth = etiqueta("bluetooth");
        Etiqueta inalambrico = etiqueta("inalámbrico");
        etiquetaRepository.saveAll(List.of(oferta, nuevo, premium, economico, bluetooth, inalambrico));

        // ---- Productos
        Producto ideapad = producto("Laptop Lenovo IdeaPad 15", "Ryzen 5, 8 GB RAM, SSD 512 GB, pantalla 15.6\"",
                "2499.00", 12, "Lenovo", laptops, oferta);
        Producto macbook = producto("MacBook Air M2 13\"", "Chip M2, 8 GB RAM, SSD 256 GB",
                "4999.00", 6, "Apple", laptops, premium, nuevo);
        Producto pavilion = producto("Laptop HP Pavilion 14", "Core i5, 16 GB RAM, SSD 512 GB",
                "2899.90", 9, "HP", laptops);
        Producto galaxy = producto("Samsung Galaxy A54 5G", "128 GB, cámara de 50 MP, pantalla AMOLED 120 Hz",
                "1599.00", 25, "Samsung", smartphones, oferta);
        Producto iphone = producto("iPhone 15 128GB", "Chip A16 Bionic, cámara de 48 MP, USB-C",
                "3899.00", 10, "Apple", smartphones, premium, nuevo);
        Producto redmi = producto("Xiaomi Redmi Note 13", "128 GB, batería de 5000 mAh",
                "799.00", 40, "Xiaomi", smartphones, economico, oferta);
        Producto sony = producto("Audífonos Sony WH-1000XM5", "Cancelación de ruido, 30 h de batería",
                "1399.00", 8, "Sony", audio, premium, inalambrico, bluetooth);
        Producto jbl = producto("Parlante JBL Flip 6", "Resistente al agua IP67, 12 h de batería",
                "449.00", 20, "JBL", audio, bluetooth, inalambrico);
        Producto basicos = producto("Audífonos con cable básicos", "Jack 3.5 mm con micrófono",
                "39.90", 100, "Genérica", audio, economico);
        Producto mouse = producto("Mouse Logitech G203", "6 botones, 8000 DPI, iluminación RGB",
                "119.00", 35, "Logitech", gaming);
        Producto teclado = producto("Teclado mecánico Redragon Kumara", "Switch rojo, formato TKL, retroiluminado",
                "189.00", 18, "Redragon", gaming, oferta);
        Producto control = producto("Control Xbox Wireless", "Compatible con PC, Xbox y móviles",
                "279.00", 15, "Microsoft", gaming, inalambrico, nuevo);
        Producto rtx = producto("Tarjeta de video RTX 4060", "8 GB GDDR6 (agotado: sirve para probar stock 0)",
                "1799.00", 0, "NVIDIA", gaming, premium);
        Producto cargador = producto("Cargador USB-C 65W", "Carga rápida GaN para laptop y celular",
                "99.00", 50, "Anker", accesorios, nuevo);
        Producto mochila = producto("Mochila para laptop 15.6\"", "Impermeable, con puerto USB",
                "129.00", 30, "Targus", accesorios);
        Producto hdmi = producto("Cable HDMI 2.1 de 2 m", "8K a 60 Hz, 4K a 120 Hz",
                "45.00", 60, "UGREEN", accesorios, economico);
        List<Producto> productos = List.of(ideapad, macbook, pavilion, galaxy, iphone, redmi, sony, jbl,
                basicos, mouse, teclado, control, rtx, cargador, mochila, hdmi);

        // ---- Usuarios y perfiles (contraseña de todos: Demo12345)
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        Usuario maria = usuario("María Quispe", "maria.quispe@demo.local", "71234567",
                "Av. Arequipa 1234, Lima", "+51987654321", hash);
        Usuario carlos = usuario("Carlos Huamán", "carlos.huaman@demo.local", "72345678",
                "Jr. de la Unión 456, Lima", "+51976543210", hash);
        Usuario lucia = usuario("Lucía Torres", "lucia.torres@demo.local", "73456789",
                "Calle Los Olivos 789, Arequipa", "+51965432109", hash);
        perfilRepository.saveAll(List.of(
                perfil(maria, "Amante de la tecnología y el café.", "https://i.pravatar.cc/300?img=47"),
                perfil(carlos, "Gamer y fan del fútbol.", "https://i.pravatar.cc/300?img=12")));

        // ---- Ventas (descuentan stock en memoria; los productos se guardan con su stock final)
        List<Venta> ventas = new ArrayList<>();
        ventas.add(venta("DEMO-0001", maria, 14, 3, new Linea(ideapad, 1), new Linea(mouse, 2)));
        ventas.add(venta("DEMO-0002", carlos, 10, 8, new Linea(galaxy, 1)));
        ventas.add(venta("DEMO-0003", maria, 6, 1, new Linea(jbl, 1), new Linea(hdmi, 3)));
        ventas.add(venta("DEMO-0004", lucia, 3, 5, new Linea(iphone, 1), new Linea(cargador, 1)));
        ventas.add(venta("DEMO-0005", carlos, 1, 2, new Linea(basicos, 2)));

        productoRepository.saveAll(productos);
        ventaRepository.saveAll(ventas);

        log.info("Datos demo cargados: 5 categorías, 6 etiquetas, {} productos, 3 usuarios (clave: {}), {} ventas",
                productos.size(), DEMO_PASSWORD, ventas.size());
    }

    // ------------------------------------------------------------------ helpers

    private record Linea(Producto producto, int cantidad) {
    }

    private Categoria categoria(String nombre, String descripcion) {
        return Categoria.builder().nombre(nombre).descripcion(descripcion).build();
    }

    private Etiqueta etiqueta(String nombre) {
        return Etiqueta.builder().nombre(nombre).build();
    }

    private Producto producto(String nombre, String descripcion, String precio, int stock, String marca,
                              Categoria categoria, Etiqueta... etiquetas) {
        return Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(new BigDecimal(precio))
                .stock(stock)
                .marca(marca)
                .categoria(categoria)
                .etiquetas(new LinkedHashSet<>(List.of(etiquetas)))
                .build();
    }

    /** Si el email ya existe se reutiliza ese usuario en lugar de fallar por duplicado. */
    private Usuario usuario(String nombre, String email, String dni, String direccion, String telefono,
                            String passwordHash) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .nombre(nombre).email(email).password(passwordHash).rol("USER")
                        .dni(dni).direccion(direccion).numeroTelefono(telefono).build()));
    }

    private Perfil perfil(Usuario usuario, String biografia, String avatarUrl) {
        return Perfil.builder().usuario(usuario).biografia(biografia).avatarUrl(avatarUrl).build();
    }

    private Venta venta(String numeroOrden, Usuario usuario, int diasAtras, int horasAtras, Linea... lineas) {
        Venta venta = Venta.builder()
                .numeroOrden(numeroOrden)
                .usuario(usuario)
                .fecha(Instant.now().minus(Duration.ofDays(diasAtras)).minus(Duration.ofHours(horasAtras)))
                .build();
        BigDecimal total = BigDecimal.ZERO;
        int items = 0;
        for (Linea linea : lineas) {
            Producto p = linea.producto();
            if (p.getStock() < linea.cantidad()) {
                throw new IllegalStateException("Datos demo inconsistentes: sin stock para " + p.getNombre());
            }
            p.setStock(p.getStock() - linea.cantidad());
            BigDecimal subtotal = p.getPrecio().multiply(BigDecimal.valueOf(linea.cantidad()));
            venta.getDetalles().add(DetalleVenta.builder()
                    .venta(venta).producto(p).cantidad(linea.cantidad())
                    .precioUnitario(p.getPrecio()).subtotal(subtotal).build());
            total = total.add(subtotal);
            items += linea.cantidad();
        }
        venta.setTotal(total);
        venta.setCantidadItems(items);
        return venta;
    }
}
