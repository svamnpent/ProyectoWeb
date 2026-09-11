package com.phantom.api.controller;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.service.DetalleVentaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class DetalleVentaControllerTest {

    private final DetalleVentaService detalleVentaService = Mockito.mock(DetalleVentaService.class);
    private final DetalleVentaController detalleVentaController = new DetalleVentaController(detalleVentaService);

    @Test
    void obtenerPorVentaIdTest() {
        Mockito.when(detalleVentaService.obtenerPorVentaId(1L)).thenReturn(List.of(new DetalleVentaDTO()));
        ResponseEntity<List<DetalleVentaDTO>> response = detalleVentaController.obtenerPorVentaId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void crearTest() {
        DetalleVentaRequestDTO request = new DetalleVentaRequestDTO();
        DetalleVentaDTO dto = new DetalleVentaDTO();
        Mockito.when(detalleVentaService.crear(eq(1L), any(DetalleVentaRequestDTO.class))).thenReturn(dto);

        ResponseEntity<DetalleVentaDTO> response = detalleVentaController.crear(1L, request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}