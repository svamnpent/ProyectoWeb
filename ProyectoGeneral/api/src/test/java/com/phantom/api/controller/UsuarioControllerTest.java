package com.phantom.api.controller;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.UsuarioRequestDTO;
import com.phantom.api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class UsuarioControllerTest {

    private final UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
    private final UsuarioController usuarioController = new UsuarioController(usuarioService);

    @Test
    void obtenerTodasTest() {
        Mockito.when(usuarioService.obtenerTodas()).thenReturn(List.of(new UsuarioDTO()));
        ResponseEntity<List<UsuarioDTO>> response = usuarioController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void crearTest() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        UsuarioDTO dto = new UsuarioDTO();
        Mockito.when(usuarioService.crear(any(UsuarioRequestDTO.class))).thenReturn(dto);

        ResponseEntity<UsuarioDTO> response = usuarioController.crear(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}