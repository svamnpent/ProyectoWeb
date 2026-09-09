package com.phantom.api.service.impl;

import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.request.CategoriaRequestDTO;
import com.phantom.api.entity.Categoria;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;
    private CategoriaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Electrónica")
                .descripcion("Dispositivos y gadgets")
                .build();

        requestDTO = CategoriaRequestDTO.builder()
                .nombre("Electrónica")
                .descripcion("Dispositivos y gadgets")
                .build();
    }

    @Test
    @DisplayName("obtenerTodas - Debe retornar lista de DTOs cuando hay datos")
    void obtenerTodas_DebeRetornarListaDeCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<CategoriaDTO> resultado = categoriaService.obtenerTodas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Electrónica", resultado.get(0).getNombre());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerPorId - Debe retornar DTO cuando existe la categoría")
    void obtenerPorId_CuandoExiste_DebeRetornarCategoriaDTO() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaDTO resultado = categoriaService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Electrónica", resultado.getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando no existe")
    void obtenerPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoriaService.obtenerPorId(99L));

        assertEquals("Categoría no encontrada con el ID: 99", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("buscarPorNombre - Debe filtrar categorías por palabra clave")
    void buscarPorNombre_DebeRetornarListaFiltrada() {
        when(categoriaRepository.findByNombreContainingIgnoreCase("elec"))
                .thenReturn(List.of(categoria));

        List<CategoriaDTO> resultado = categoriaService.buscarPorNombre("elec");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Electrónica", resultado.get(0).getNombre());
        verify(categoriaRepository, times(1)).findByNombreContainingIgnoreCase("elec");
    }

    @Test
    @DisplayName("crear - Debe guardar y retornar DTO cuando el nombre no está duplicado")
    void crear_ConNombreNuevo_DebeGuardarExitosamente() {
        when(categoriaRepository.existsByNombreIgnoreCase("Electrónica")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaDTO resultado = categoriaService.crear(requestDTO);

        assertNotNull(resultado);
        assertEquals("Electrónica", resultado.getNombre());
        verify(categoriaRepository, times(1)).existsByNombreIgnoreCase("Electrónica");
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    @DisplayName("crear - Debe lanzar IllegalArgumentException si el nombre ya existe")
    void crear_ConNombreDuplicado_DebeLanzarExcepcion() {
        when(categoriaRepository.existsByNombreIgnoreCase("Electrónica")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.crear(requestDTO));

        assertTrue(exception.getMessage().contains("Ya existe una categoría registrada"));
        verify(categoriaRepository, times(1)).existsByNombreIgnoreCase("Electrónica");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - Debe actualizar datos correctamente si el nombre no cambia")
    void actualizar_MismoNombre_DebeActualizarSinValidarDuplicado() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaDTO resultado = categoriaService.actualizar(1L, requestDTO);

        assertNotNull(resultado);
        assertEquals("Electrónica", resultado.getNombre());
        verify(categoriaRepository, never()).existsByNombreIgnoreCase(anyString());
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    @DisplayName("actualizar - Debe actualizar datos si cambia de nombre a uno no ocupado")
    void actualizar_NuevoNombreValido_DebeActualizarExitosamente() {
        CategoriaRequestDTO nuevoRequest = CategoriaRequestDTO.builder()
                .nombre("Hogar")
                .descripcion("Artículos para el hogar")
                .build();

        Categoria categoriaActualizada = Categoria.builder()
                .id(1L)
                .nombre("Hogar")
                .descripcion("Artículos para el hogar")
                .build();

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Hogar")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaActualizada);

        CategoriaDTO resultado = categoriaService.actualizar(1L, nuevoRequest);

        assertNotNull(resultado);
        assertEquals("Hogar", resultado.getNombre());
        verify(categoriaRepository, times(1)).existsByNombreIgnoreCase("Hogar");
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    @DisplayName("actualizar - Debe lanzar IllegalArgumentException si cambia a un nombre ya existente")
    void actualizar_NuevoNombreDuplicado_DebeLanzarExcepcion() {
        CategoriaRequestDTO nuevoRequest = CategoriaRequestDTO.builder()
                .nombre("Ropa")
                .descripcion("Prenda de vestir")
                .build();

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Ropa")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.actualizar(1L, nuevoRequest));

        assertTrue(exception.getMessage().contains("Ya existe otra categoría"));
        verify(categoriaRepository, times(1)).existsByNombreIgnoreCase("Ropa");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar - Debe eliminar categoría si el ID existe")
    void eliminar_CuandoExiste_DebeEliminarExitosamente() {
        when(categoriaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoriaRepository).deleteById(1L);

        assertDoesNotThrow(() -> categoriaService.eliminar(1L));

        verify(categoriaRepository, times(1)).existsById(1L);
        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar - Debe lanzar ResourceNotFoundException si el ID no existe")
    void eliminar_CuandoNoExiste_DebeLanzarExcepcion() {
        when(categoriaRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoriaService.eliminar(99L));

        assertTrue(exception.getMessage().contains("No se puede eliminar"));
        verify(categoriaRepository, times(1)).existsById(99L);
        verify(categoriaRepository, never()).deleteById(any());
    }
}