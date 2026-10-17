package com.phantom.api.controller;

import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TIENDA - lectura pública de categorías.
 * Crear/editar/eliminar está en BackOfficeController.
 */
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> obtenerTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaDTO>> buscarPorNombre(@RequestParam String keyword) {
        return ResponseEntity.ok(categoriaService.buscarPorNombre(keyword));
    }
}
