package com.phantom.api.controller;

import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TIENDA - catálogo público de productos (lectura).
 * Crear/editar/eliminar está en BackOfficeController.
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> obtenerTodas() {
        return ResponseEntity.ok(productoService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO>> buscarPorPalabraClave(@RequestParam String keyword) {
        return ResponseEntity.ok(productoService.buscarPorPalabraClave(keyword));
    }
}
