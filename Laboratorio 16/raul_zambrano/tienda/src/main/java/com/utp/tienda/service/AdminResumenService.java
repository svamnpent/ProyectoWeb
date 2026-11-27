package com.utp.tienda.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.tienda.dto.ResumenBackendResponse;
import com.utp.tienda.repository.MovimientoStockRepository;
import com.utp.tienda.repository.ProductoRepository;

@Service
public class AdminResumenService {

	private final ProductoRepository productoRepository;
	private final MovimientoStockRepository movimientoRepository;

	public AdminResumenService(ProductoRepository productoRepository,
			MovimientoStockRepository movimientoRepository) {
		this.productoRepository = productoRepository;
		this.movimientoRepository = movimientoRepository;
	}

	@Transactional(readOnly = true)
	public ResumenBackendResponse obtenerResumen() {
		long total = productoRepository.count();
		long bajo = productoRepository.countByStockLessThan(5);
		long movimientos = movimientoRepository.count();

		return new ResumenBackendResponse(
				total,
				bajo,
				movimientos,
				"BACKEND_OPERATIVO");
	}
}
