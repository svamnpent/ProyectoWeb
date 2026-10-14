package com.phantom.api.service.impl;

import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.request.EtiquetaRequestDTO;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.EtiquetaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtiquetaServiceImplTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @InjectMocks
    private EtiquetaServiceImpl service;

    private final Etiqueta etiqueta = Etiqueta.builder().id(1L).nombre("oferta").build();
    private final EtiquetaRequestDTO request = EtiquetaRequestDTO.builder().nombre(" oferta ").build();

    @Test
    @DisplayName("obtenerTodas / obtenerPorId")
    void lecturas() {
        when(etiquetaRepository.findAll()).thenReturn(List.of(etiqueta));
        when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));

        assertEquals(1, service.obtenerTodas().size());
        assertEquals("oferta", service.obtenerPorId(1L).getNombre());
    }

    @Test
    @DisplayName("obtenerPorId - inexistente")
    void obtenerPorId_inexistente() {
        when(etiquetaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(9L));
    }

    @Test
    @DisplayName("crear - guarda con el nombre recortado")
    void crear_ok() {
        when(etiquetaRepository.existsByNombreIgnoreCase("oferta")).thenReturn(false);
        when(etiquetaRepository.save(any(Etiqueta.class))).thenReturn(etiqueta);

        EtiquetaDTO dto = service.crear(request);

        assertEquals("oferta", dto.getNombre());
    }

    @Test
    @DisplayName("crear - nombre duplicado lanza IllegalArgumentException")
    void crear_duplicada() {
        when(etiquetaRepository.existsByNombreIgnoreCase("oferta")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        verify(etiquetaRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - a un nombre ya usado lanza IllegalArgumentException")
    void actualizar_duplicada() {
        when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));
        when(etiquetaRepository.existsByNombreIgnoreCase("nueva")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, EtiquetaRequestDTO.builder().nombre("nueva").build()));
        verify(etiquetaRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - mismo nombre no valida duplicados")
    void actualizar_mismoNombre() {
        when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));
        when(etiquetaRepository.save(etiqueta)).thenReturn(etiqueta);

        service.actualizar(1L, request);

        verify(etiquetaRepository, never()).existsByNombreIgnoreCase(any());
    }

    @Test
    @DisplayName("eliminar - existente e inexistente")
    void eliminar() {
        when(etiquetaRepository.existsById(1L)).thenReturn(true);
        when(etiquetaRepository.existsById(9L)).thenReturn(false);

        service.eliminar(1L);
        verify(etiquetaRepository).deleteById(1L);
        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(9L));
    }
}
