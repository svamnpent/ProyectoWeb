package com.phantom.api.controller;

import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.service.EtiquetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TIENDA - lectura pública de etiquetas.
 * Crear/editar/eliminar está en BackOfficeController.
 */
@RestController
@RequestMapping("/api/v1/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    @GetMapping
    public ResponseEntity<List<EtiquetaDTO>> obtenerTodas() {
        return ResponseEntity.ok(etiquetaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtiquetaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(etiquetaService.obtenerPorId(id));
    }
}
