package com.utp.productosapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utp.productosapi.model.Producto;
import com.utp.productosapi.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
	@Mock
	private ProductoRepository repository;

	@InjectMocks
	private ProductoService service;

	@Test
	void debeCrearProductoYAsignarId() {
		Producto nuevo = new Producto(null, "Laptop", 3500.0, 10, "Computo");
		when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
			Producto producto = invocation.getArgument(0);
			producto.setId(1L);
			return producto;
		});

		Producto creado = service.crear(nuevo);

		assertNotNull(creado.getId());
		assertEquals("Laptop", creado.getNombre());
		assertEquals(3500.0, creado.getPrecio());
		assertEquals("Computo", creado.getCategoria());
	}

	@Test
	void debeBuscarProductoPorId() {
		Producto existente = new Producto(1L, "Mouse", 80.0, 20, "Accesorios");
		when(repository.findById(1L)).thenReturn(Optional.of(existente));

		Producto encontrado = service.buscarPorId(1L).orElseThrow();

		assertEquals("Mouse", encontrado.getNombre());
	}

	@Test
	void debeRechazarPrecioInvalido() {
		Producto invalido = new Producto(null, "Monitor", 0.0, 5, "Perifericos");

		assertThrows(IllegalArgumentException.class, () -> service.crear(invalido));
		verify(repository, never()).save(any());
	}

	@Test
	void debeEliminarProducto() {
		when(repository.existsById(1L)).thenReturn(true);
		when(repository.findById(1L)).thenReturn(Optional.empty());

		boolean eliminado = service.eliminar(1L);

		assertTrue(eliminado);
		assertTrue(service.buscarPorId(1L).isEmpty());
		verify(repository).deleteById(1L);
	}
}
