package com.utp.semana3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.utp.semana3.model.Producto;

class ProductoServiceTest {

    @Test
    void registrarProductoValido_debeAsignarIdYGuardar() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Laptop", 3500.00, 10);
        Producto registrado = service.registrar(producto);
        assertThat(registrado.getId()).isNotNull();
        assertThat(registrado.getNombre()).isEqualTo("Laptop");
        assertThat(service.listar()).hasSize(1);
    }

    @Test
    void registrarProductoConNombreNuloOVacio_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(new Producto(null, null, 100.0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(new Producto(null, "   ", 100.0, 1)));
    }

    @Test
    void registrarProductoConPrecioInvalido_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(new Producto(null, "Mouse", null, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(new Producto(null, "Mouse", 0.0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(new Producto(null, "Mouse", -5.0, 1)));
    }

    @Test
    void registrarProductoConStockNegativo_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(new Producto(null, "Teclado", 50.0, -1)));
    }
}

