package com.utp.semana3.service;

import com.utp.semana3.model.Producto;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

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
    void registrarProductoConPrecioCero_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Mouse", 0.00, 5);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser mayor que cero");
    }

    @Test
    void registrarProductoSinNombre_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "", 100.00, 5);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre es obligatorio");
    }

    @Test
    void registrarProductoConStockNegativo_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Teclado", 150.00, -1);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El stock no puede ser negativo");
    }

}