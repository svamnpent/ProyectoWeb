package com.phantom.api.controller;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.service.DetalleVentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/detalle-ventas")
@RequiredArgsConstructor
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<DetalleVentaDTO>> obtenerPorVenta(@PathVariable Long ventaId) {
        return ResponseEntity.ok(detalleVentaService.obtenerPorVentaId(ventaId));
    }

    @PostMapping("/venta/{ventaId}")
    public ResponseEntity<DetalleVentaDTO> crear(@PathVariable Long ventaId, @Valid @RequestBody DetalleVentaRequestDTO request) {
        return ResponseEntity.ok(detalleVentaService.crear(ventaId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        detalleVentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
