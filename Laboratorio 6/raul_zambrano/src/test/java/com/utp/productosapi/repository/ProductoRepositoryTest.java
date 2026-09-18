package com.utp.productosapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.utp.productosapi.model.Producto;

@DataJpaTest
class ProductoRepositoryTest {
	@Autowired
	private ProductoRepository repository;

	@Test
	void debeGuardarYRecuperarProducto() {
		Producto producto = new Producto(null, "Monitor", 900.0, 5, "Perifericos");
		Producto guardado = repository.save(producto);
		assertThat(guardado.getId()).isNotNull();
		assertThat(repository.findById(guardado.getId())).isPresent();
	}

	@Test
	void debePersistirCategoria() {
		Producto computo = repository.save(new Producto(null, "Laptop", 3500.0, 10, "Computo"));
		Producto oficina = repository.save(new Producto(null, "Lapicero", 2.5, 50, "Oficina"));

		assertThat(repository.findById(computo.getId()).orElseThrow().getCategoria()).isEqualTo("Computo");
		assertThat(repository.findById(oficina.getId()).orElseThrow().getCategoria()).isEqualTo("Oficina");
	}
}
