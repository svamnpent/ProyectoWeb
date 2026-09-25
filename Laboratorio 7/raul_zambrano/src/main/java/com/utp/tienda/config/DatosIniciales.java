package com.utp.tienda.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.utp.tienda.model.Producto;
import com.utp.tienda.repository.ProductoRepository;

@Component
public class DatosIniciales implements CommandLineRunner {
	private final ProductoRepository productoRepository;

	public DatosIniciales(ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	@Override
	public void run(String... args) {
		if (productoRepository.count() > 0) {
			return;
		}
		productoRepository.saveAll(List.of(
				new Producto("Laptop Lenovo ThinkPad", "Tecnologia", new BigDecimal("4200.00"), 8),
				new Producto("Mouse Logitech MX", "Tecnologia", new BigDecimal("320.00"), 20),
				new Producto("Silla ergonomica", "Muebles", new BigDecimal("850.00"), 6),
				new Producto("Escritorio ejecutivo", "Muebles", new BigDecimal("1200.00"), 4),
				new Producto("Monitor 27 pulgadas", "Tecnologia", new BigDecimal("1450.00"), 10)));
	}
}
