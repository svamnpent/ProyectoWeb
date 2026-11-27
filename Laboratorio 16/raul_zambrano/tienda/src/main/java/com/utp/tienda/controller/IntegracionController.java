package com.utp.tienda.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.tienda.dto.RespuestaIntegracion;
import com.utp.tienda.dto.SolicitudIntegracion;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/diagnostico")
public class IntegracionController {

	@GetMapping("/estado")
	public Map<String, String> estado() {
		return Map.of(
				"estado", "OPERATIVO",
				"servicio", "Tienda UTP API",
				"fecha", LocalDate.now().toString());
	}

	@PostMapping("/validar")
	public RespuestaIntegracion validar(@Valid @RequestBody SolicitudIntegracion solicitud) {
		return new RespuestaIntegracion(
				"Datos recibidos y validados",
				solicitud.nombre(),
				solicitud.monto(),
				solicitud.fecha(),
				solicitud.correo());
	}
}
