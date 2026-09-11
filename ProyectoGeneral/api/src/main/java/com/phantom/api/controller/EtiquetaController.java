package com.phantom.api.controller;

import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.request.EtiquetaRequestDTO;
import com.phantom.api.service.EtiquetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<EtiquetaDTO> crear(@Valid @RequestBody EtiquetaRequestDTO request) {
        return new ResponseEntity<>(etiquetaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EtiquetaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EtiquetaRequestDTO request) {
        return ResponseEntity.ok(etiquetaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        etiquetaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
