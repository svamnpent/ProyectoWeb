package com.phantom.api.controller;

import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.service.VentaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class VentaControllerTest {

    private final VentaService ventaService = Mockito.mock(VentaService.class);
    private final VentaController ventaController = new VentaController(ventaService);

    @Test
    void obtenerTodasTest() {
        Mockito.when(ventaService.obtenerTodas()).thenReturn(List.of(new VentaDTO()));
        ResponseEntity<List<VentaDTO>> response = ventaController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void crearTest() {
        VentaRequestDTO request = new VentaRequestDTO();
        VentaDTO dto = new VentaDTO();
        Mockito.when(ventaService.crear(any(VentaRequestDTO.class))).thenReturn(dto);

        ResponseEntity<VentaDTO> response = ventaController.crear(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}