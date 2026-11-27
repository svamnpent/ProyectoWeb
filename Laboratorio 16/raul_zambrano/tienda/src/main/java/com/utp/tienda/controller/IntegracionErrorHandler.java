package com.utp.tienda.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.utp.tienda.dto.ApiError;

@RestControllerAdvice
public class IntegracionErrorHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
		String detalle = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.orElse("Datos inválidos");
		return ResponseEntity.badRequest().body(
				new ApiError("VALIDACION", detalle, LocalDateTime.now()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> jsonInvalido(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(new ApiError(
				"JSON_INVALIDO",
				"Compruebe tipos de datos, formato y fecha yyyy-MM-dd",
				LocalDateTime.now()));
	}
}
