package com.utp.tienda.dto;

public record ResumenBackendResponse(
		long totalProductos,
		long productosConStockBajo,
		long totalMovimientos,
		String estado) {
}
