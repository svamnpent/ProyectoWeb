package com.utp.semana3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.utp.semana3.model.Producto;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();
    private long secuencia = 1;

    public Producto registrar(Producto producto) {
        validar(producto);
        producto.setId(secuencia++);
        productos.add(producto);
        return producto;
    }

    public List<Producto> listar() {
        return productos;
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    private void validar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }
}
