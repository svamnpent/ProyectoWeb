package com.phantom.api.controller;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.service.DetalleVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TIENDA - líneas de una venta (dueño o personal). Agregar/quitar líneas está en BackOfficeController.
 */
@RestController
@RequiredArgsConstructor
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    // Anidado bajo la venta: GET /api/v1/ventas/{ventaId}/detalles
    @GetMapping("/api/v1/ventas/{ventaId}/detalles")
    public ResponseEntity<List<DetalleVentaDTO>> obtenerPorVentaId(@PathVariable Long ventaId) {
        return ResponseEntity.ok(detalleVentaService.obtenerPorVentaId(ventaId));
    }
}
