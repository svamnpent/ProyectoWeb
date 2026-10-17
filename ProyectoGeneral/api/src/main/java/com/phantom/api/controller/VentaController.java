package com.phantom.api.controller;

import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TIENDA - el comprador compra y consulta SUS compras.
 * El listado global, la anulación de ventas y la venta de mostrador están en BackOfficeController.
 */
@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    // Historial de compras del usuario autenticado
    @GetMapping("/mias")
    public ResponseEntity<List<VentaDTO>> obtenerMisVentas() {
        return ResponseEntity.ok(ventaService.obtenerMisVentas());
    }

    // Dueño de la venta o personal de la tienda (se valida en el servicio)
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<VentaDTO> crear(@Valid @RequestBody VentaRequestDTO request) {
        return new ResponseEntity<>(ventaService.crear(request), HttpStatus.CREATED);
    }
}
