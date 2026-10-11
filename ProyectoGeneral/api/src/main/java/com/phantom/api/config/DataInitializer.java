package com.phantom.api.config;

import com.phantom.api.entity.Categoria;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Usuario;
import com.phantom.api.repository.CategoriaRepository;
import com.phantom.api.repository.EtiquetaRepository;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() > 0) {
            return;
        }

        // 1. Usuarios demo
        Usuario admin = Usuario.builder()
                .nombre("Administrador Principal")
                .email("admin@phantom.com")
                .password(passwordEncoder.encode("admin123"))
                .rol("ADMIN")
                .dni("12345678")
                .direccion("Av. Central 123")
                .numeroTelefono("987654321")
                .build();

        Usuario user = Usuario.builder()
                .nombre("Juan Perez")
                .email("juan@phantom.com")
                .password(passwordEncoder.encode("user123"))
                .rol("USER")
                .dni("87654321")
                .direccion("Calle Las Flores 456")
                .numeroTelefono("912345678")
                .build();

        usuarioRepository.save(admin);
        usuarioRepository.save(user);

        // 2. Categorías demo
        Categoria catLaptops = Categoria.builder()
                .nombre("Laptops y Computadoras")
                .descripcion("Equipos portátiles y de escritorio de alta gama")
                .build();

        Categoria catAccesorios = Categoria.builder()
                .nombre("Accesorios y Periféricos")
                .descripcion("Teclados, mouse, audífonos y monitores")
                .build();

        categoriaRepository.save(catLaptops);
        categoriaRepository.save(catAccesorios);

        // 3. Etiquetas demo
        Etiqueta tagGaming = Etiqueta.builder().nombre("Gaming").build();
        Etiqueta tagOferta = Etiqueta.builder().nombre("Oferta").build();
        Etiqueta tagPro = Etiqueta.builder().nombre("Profesional").build();

        etiquetaRepository.save(tagGaming);
        etiquetaRepository.save(tagOferta);
        etiquetaRepository.save(tagPro);

        // 4. Productos demo
        Producto prod1 = Producto.builder()
                .nombre("Laptop Gamer Asus ROG Strix")
                .descripcion("Intel Core i7 13va Gen, 16GB RAM DDR5, RTX 4060, SSD 1TB NVMe")
                .precio(new BigDecimal("4599.00"))
                .stock(15)
                .marca("ASUS")
                .categoria(catLaptops)
                .etiquetas(Set.of(tagGaming, tagPro))
                .build();

        Producto prod2 = Producto.builder()
                .nombre("Teclado Mecánico RGB Redragon Kumara")
                .descripcion("Switch Outemu Blue, retroiluminación RGB configurable, anti-ghosting")
                .precio(new BigDecimal("189.90"))
                .stock(40)
                .marca("Redragon")
                .categoria(catAccesorios)
                .etiquetas(Set.of(tagGaming, tagOferta))
                .build();

        Producto prod3 = Producto.builder()
                .nombre("Mouse Inalámbrico Logitech MX Master 3S")
                .descripcion("Sensor 8K DPI silencioso, carga rápida USB-C, conexión multidispositivo")
                .precio(new BigDecimal("399.00"))
                .stock(25)
                .marca("Logitech")
                .categoria(catAccesorios)
                .etiquetas(Set.of(tagPro))
                .build();

        productoRepository.save(prod1);
        productoRepository.save(prod2);
        productoRepository.save(prod3);
    }
}

