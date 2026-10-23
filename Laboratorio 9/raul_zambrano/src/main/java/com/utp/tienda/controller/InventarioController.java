package com.utp.tienda.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utp.tienda.model.Producto;
import com.utp.tienda.service.ProductoService;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
	private final ProductoService service;

	public InventarioController(ProductoService service) {
		this.service = service;
	}

	@GetMapping
	public List<Producto> listar() {
		return service.listar();
	}

	@GetMapping("/stock-bajo")
	public List<Producto> stockBajo(@RequestParam(defaultValue = "5") Integer limite) {
		return service.buscarConStockBajo(limite);
	}

	@PostMapping("/ajustes")
	public ResponseEntity<Producto> ajustar(@RequestBody AjusteInventarioRequest request) {
		return ResponseEntity.ok(service.ajustarInventario(
				request.productoId(), request.tipo(), request.cantidad()));
	}

	public record AjusteInventarioRequest(Long productoId, String tipo, int cantidad) {
	}
}
