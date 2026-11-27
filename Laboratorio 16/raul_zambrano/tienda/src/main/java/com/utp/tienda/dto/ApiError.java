package com.utp.tienda.dto;

import java.time.LocalDateTime;

public record ApiError(
		String codigo,
		String mensaje,
		LocalDateTime fecha) {
}
