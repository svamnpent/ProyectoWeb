package com.utp.tienda.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record SolicitudIntegracion(
		@NotBlank @Size(min = 3) String nombre,
		@NotNull @DecimalMin("0.01") BigDecimal monto,
		@NotNull @PastOrPresent LocalDate fecha,
		@Email String correo) {
}
