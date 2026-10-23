package com.utp.tienda.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.utp.tienda.model.Producto;

@DataJpaTest
class ProductoRepositoryTest {
	@Autowired
	private ProductoRepository repository;

	@BeforeEach
	void cargarDatos() {
		repository.saveAll(List.of(
				new Producto("Laptop Lenovo ThinkPad", "Tecnologia", new BigDecimal("4200.00"), 8),
				new Producto("Mouse Logitech MX", "Tecnologia", new BigDecimal("320.00"), 20),
				new Producto("Silla ergonomica", "Muebles", new BigDecimal("850.00"), 6),
				new Producto("Escritorio ejecutivo", "Muebles", new BigDecimal("1200.00"), 4)));
	}

	@Test
	void debeBuscarPorNombreSinDistinguirMayusculas() {
		List<Producto> encontrados = repository.buscarPorNombre("lap");

		assertThat(encontrados).extracting(Producto::getNombre)
				.containsExactly("Laptop Lenovo ThinkPad");
	}

	@Test
	void debeFiltrarPorCategoriaIgnoreCase() {
		List<Producto> tecnologia = repository.findByCategoriaIgnoreCase("tecnologia");

		assertThat(tecnologia).hasSize(2);
	}

	@Test
	void debeBuscarPorRangoDePrecioOrdenado() {
		List<Producto> encontrados = repository.buscarPorRangoPrecio(
				new BigDecimal("300"), new BigDecimal("1500"));

		assertThat(encontrados).extracting(Producto::getNombre)
				.containsExactly("Mouse Logitech MX", "Silla ergonomica", "Escritorio ejecutivo");
	}

	@Test
	void debeBuscarConStockBajo() {
		List<Producto> encontrados = repository.buscarConStockBajo(5);

		assertThat(encontrados).extracting(Producto::getNombre)
				.containsExactly("Escritorio ejecutivo");
	}
}
