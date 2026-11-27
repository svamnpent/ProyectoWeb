package com.utp.tienda.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RespuestaIntegracion(
		String mensaje,
		String nombre,
		BigDecimal monto,
		LocalDate fecha,
		String correo) {
}
