package com.phantom.api.controller;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.UsuarioRequestDTO;
import com.phantom.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * TIENDA - el comprador gestiona SU propia cuenta (ver y editar sus datos).
 * La gestión de todos los usuarios (listar, crear, cambiar roles, eliminar) está en BackOfficeController.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("@currentUser.isSelf(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // El servicio impide que el comprador se cambie el rol
    @PreAuthorize("@currentUser.isSelf(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }
}
