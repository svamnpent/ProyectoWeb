package com.utp.tienda.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.tienda.exception.RecursoNoEncontradoException;
import com.utp.tienda.exception.ReglaNegocioException;
import com.utp.tienda.exception.StockInsuficienteException;
import com.utp.tienda.model.MovimientoStock;
import com.utp.tienda.model.Producto;
import com.utp.tienda.repository.MovimientoStockRepository;
import com.utp.tienda.repository.ProductoRepository;

@Service
public class ProductoService {
	private final ProductoRepository productoRepository;
	private final MovimientoStockRepository movimientoRepository;

	public ProductoService(ProductoRepository productoRepository,
			MovimientoStockRepository movimientoRepository) {
		this.productoRepository = productoRepository;
		this.movimientoRepository = movimientoRepository;
	}

	@Transactional(readOnly = true)
	public List<Producto> listar() {
		return productoRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Producto buscarPorId(Long id) {
		return obtenerProducto(id);
	}

	@Transactional
	public Producto crear(Producto producto) {
		validarProducto(producto);
		producto.setId(null);
		return productoRepository.save(producto);
	}

	@Transactional(readOnly = true)
	public List<Producto> buscarPorNombre(String texto) {
		if (texto == null || texto.isBlank()) {
			throw new ReglaNegocioException("El texto de busqueda es obligatorio");
		}
		return productoRepository.buscarPorNombre(texto.trim());
	}

	@Transactional(readOnly = true)
	public List<Producto> buscarPorCategoria(String categoria) {
		return productoRepository.findByCategoriaIgnoreCase(categoria);
	}

	@Transactional(readOnly = true)
	public List<Producto> buscarPorRango(BigDecimal min, BigDecimal max) {
		if (min.compareTo(max) > 0) {
			throw new ReglaNegocioException("El precio minimo no puede superar al maximo");
		}
		return productoRepository.buscarPorRangoPrecio(min, max);
	}

	@Transactional(readOnly = true)
	public List<Producto> buscarConStockBajo(Integer limite) {
		if (limite == null || limite < 0) {
			throw new ReglaNegocioException("El limite de stock debe ser mayor o igual a cero");
		}
		return productoRepository.buscarConStockBajo(limite);
	}

	@Transactional
	public Producto registrarSalida(Long productoId, int cantidad) {
		Producto producto = obtenerProducto(productoId);
		validarSalida(producto, cantidad);
		producto.setStock(producto.getStock() - cantidad);
		MovimientoStock movimiento = new MovimientoStock(
				producto, "SALIDA", cantidad, LocalDateTime.now());
		movimientoRepository.save(movimiento);
		return producto;
	}

	@Transactional
	public Producto registrarEntrada(Long productoId, int cantidad) {
		if (cantidad <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
		}
		Producto producto = obtenerProducto(productoId);
		producto.setStock(producto.getStock() + cantidad);
		movimientoRepository.save(new MovimientoStock(
				producto, "ENTRADA", cantidad, LocalDateTime.now()));
		return producto;
	}

	@Transactional
	public Producto actualizar(Long id, Producto cambios) {
		Producto producto = obtenerProducto(id);
		validarProducto(cambios);
		producto.setNombre(cambios.getNombre());
		producto.setCategoria(cambios.getCategoria());
		producto.setPrecio(cambios.getPrecio());
		producto.setStock(cambios.getStock());
		return producto;
	}

	@Transactional
	public Producto actualizarPrecio(Long id, BigDecimal precio) {
		if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("El precio debe ser mayor que cero");
		}
		Producto producto = obtenerProducto(id);
		producto.setPrecio(precio);
		return producto;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public void eliminar(Long id) {
		productoRepository.delete(obtenerProducto(id));
	}

	@Transactional
	public Producto ajustarInventario(Long productoId, String tipo, int cantidad) {
		if (tipo == null || tipo.isBlank()) {
			throw new ReglaNegocioException("El tipo de ajuste es obligatorio");
		}
		if ("ENTRADA".equalsIgnoreCase(tipo.trim())) {
			return registrarEntrada(productoId, cantidad);
		}
		if ("SALIDA".equalsIgnoreCase(tipo.trim())) {
			return registrarSalida(productoId, cantidad);
		}
		throw new ReglaNegocioException("El tipo de ajuste debe ser ENTRADA o SALIDA");
	}

	@Transactional
	public void simularSalidaConError(Long productoId, int cantidad) {
		Producto producto = obtenerProducto(productoId);
		validarSalida(producto, cantidad);
		producto.setStock(producto.getStock() - cantidad);
		movimientoRepository.save(new MovimientoStock(
				producto, "SALIDA", cantidad, LocalDateTime.now()));
		throw new IllegalStateException(
				"Error simulado: la transaccion debe hacer rollback");
	}

	private Producto obtenerProducto(Long id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"Producto no encontrado: " + id));
	}

	private void validarSalida(Producto producto, int cantidad) {
		if (cantidad <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
		}
		if (producto.getStock() < cantidad) {
			throw new StockInsuficienteException(
					"Stock actual: " + producto.getStock() + ", solicitado: " + cantidad);
		}
	}

	private void validarProducto(Producto producto) {
		if (producto.getNombre() == null || producto.getNombre().isBlank()) {
			throw new ReglaNegocioException("El nombre es obligatorio");
		}
		if (producto.getCategoria() == null || producto.getCategoria().isBlank()) {
			throw new ReglaNegocioException("La categoria es obligatoria");
		}
		if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("El precio debe ser mayor que cero");
		}
		if (producto.getStock() == null || producto.getStock() < 0) {
			throw new ReglaNegocioException("El stock no puede ser negativo");
		}
	}
}
