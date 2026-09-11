package com.phantom.api.controller;

import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.dto.request.ProductoRequestDTO;
import com.phantom.api.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ProductoControllerTest {

    private final ProductoService productoService = Mockito.mock(ProductoService.class);
    private final ProductoController productoController = new ProductoController(productoService);

    @Test
    void obtenerTodasTest() {
        Mockito.when(productoService.obtenerTodas()).thenReturn(List.of(new ProductoDTO()));
        ResponseEntity<List<ProductoDTO>> response = productoController.obtenerTodas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void crearTest() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        ProductoDTO dto = new ProductoDTO();
        Mockito.when(productoService.crear(any(ProductoRequestDTO.class))).thenReturn(dto);

        ResponseEntity<ProductoDTO> response = productoController.crear(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}