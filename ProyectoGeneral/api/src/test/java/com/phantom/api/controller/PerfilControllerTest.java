package com.phantom.api.controller;

import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.request.PerfilRequestDTO;
import com.phantom.api.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class PerfilControllerTest {

    private final PerfilService perfilService = Mockito.mock(PerfilService.class);
    private final PerfilController perfilController = new PerfilController(perfilService);

    @Test
    void obtenerTodosTest() {
        Mockito.when(perfilService.obtenerTodos()).thenReturn(List.of(new PerfilDTO()));
        ResponseEntity<List<PerfilDTO>> response = perfilController.obtenerTodos();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void crearTest() {
        PerfilRequestDTO request = new PerfilRequestDTO();
        PerfilDTO dto = new PerfilDTO();
        Mockito.when(perfilService.crear(any(PerfilRequestDTO.class))).thenReturn(dto);

        ResponseEntity<PerfilDTO> response = perfilController.crear(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}