package com.phantom.api.controller;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.service.DetalleVentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    // Anidado bajo la venta: GET /api/v1/ventas/{ventaId}/detalles
    @GetMapping("/api/v1/ventas/{ventaId}/detalles")
    public ResponseEntity<List<DetalleVentaDTO>> obtenerPorVentaId(@PathVariable Long ventaId) {
        return ResponseEntity.ok(detalleVentaService.obtenerPorVentaId(ventaId));
    }

    // POST /api/v1/ventas/{ventaId}/detalles
    @PostMapping("/api/v1/ventas/{ventaId}/detalles")
    public ResponseEntity<DetalleVentaDTO> crear(
            @PathVariable Long ventaId,
            @Valid @RequestBody DetalleVentaRequestDTO request) {
        return new ResponseEntity<>(detalleVentaService.crear(ventaId, request), HttpStatus.CREATED);
    }

    // DELETE /api/v1/detalles/{id}
    @DeleteMapping("/api/v1/detalles/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        detalleVentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
