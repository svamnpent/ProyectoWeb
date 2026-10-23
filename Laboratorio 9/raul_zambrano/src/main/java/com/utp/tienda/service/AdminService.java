package com.utp.tienda.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.tienda.repository.ProductoRepository;

@Service
public class AdminService {
	private final ProductoRepository productoRepository;

	public AdminService(ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> reporte() {
		return Map.of(
				"fechaServidor", Instant.now().toString(),
				"totalProductos", productoRepository.count());
	}
}
