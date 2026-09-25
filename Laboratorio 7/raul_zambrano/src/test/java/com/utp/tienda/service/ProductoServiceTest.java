package com.utp.tienda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utp.tienda.exception.ReglaNegocioException;
import com.utp.tienda.model.MovimientoStock;
import com.utp.tienda.model.Producto;
import com.utp.tienda.repository.MovimientoStockRepository;
import com.utp.tienda.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
	@Mock
	private ProductoRepository productoRepository;

	@Mock
	private MovimientoStockRepository movimientoRepository;

	@InjectMocks
	private ProductoService service;

	@Test
	void debeRegistrarSalidaYDescontarStock() {
		Producto producto = producto(1L, 8);
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

		Producto actualizado = service.registrarSalida(1L, 2);

		assertEquals(6, actualizado.getStock());
		verify(movimientoRepository).save(any(MovimientoStock.class));
	}

	@Test
	void debeRegistrarEntradaYAumentarStock() {
		Producto producto = producto(1L, 8);
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

		Producto actualizado = service.registrarEntrada(1L, 3);

		assertEquals(11, actualizado.getStock());
		verify(movimientoRepository).save(any(MovimientoStock.class));
	}

	@Test
	void debeRechazarSalidaConStockInsuficiente() {
		Producto producto = producto(1L, 1);
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

		assertThrows(ReglaNegocioException.class, () -> service.registrarSalida(1L, 2));
		verify(movimientoRepository, never()).save(any());
	}

	@Test
	void debeRechazarTextoDeBusquedaVacio() {
		assertThrows(ReglaNegocioException.class, () -> service.buscarPorNombre("   "));
	}

	@Test
	void debeRechazarRangoDePrecioInvertido() {
		assertThrows(ReglaNegocioException.class,
				() -> service.buscarPorRango(new BigDecimal("2000"), new BigDecimal("100")));
	}

	@Test
	void simularErrorDebeLanzarExcepcion() {
		Producto producto = producto(1L, 8);
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

		assertThrows(IllegalStateException.class, () -> service.simularSalidaConError(1L, 1));
		verify(movimientoRepository).save(any(MovimientoStock.class));
	}

	private Producto producto(Long id, int stock) {
		Producto producto = new Producto("Laptop", "Tecnologia", new BigDecimal("4200.00"), stock);
		producto.setId(id);
		return producto;
	}
}
