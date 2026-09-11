package com.phantom.api.controller;

import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.request.PerfilRequestDTO;
import com.phantom.api.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/perfiles")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public ResponseEntity<List<PerfilDTO>> obtenerTodas() {
        return ResponseEntity.ok(perfilService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PerfilDTO> crear(@Valid @RequestBody PerfilRequestDTO request) {
        return ResponseEntity.ok(perfilService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerfilDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequestDTO request) {
        return ResponseEntity.ok(perfilService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        perfilService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
