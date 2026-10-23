package com.utp.tienda.exception;

public class StockInsuficienteException extends RuntimeException {
	public StockInsuficienteException(String mensaje) {
		super(mensaje);
	}
}
