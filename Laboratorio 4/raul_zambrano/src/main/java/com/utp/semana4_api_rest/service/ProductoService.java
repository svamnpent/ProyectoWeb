package com.utp.semana4_api_rest.service;

import com.utp.semana4_api_rest.exception.ProductoNoEncontradoException;
import com.utp.semana4_api_rest.model.Producto;
import com.utp.semana4_api_rest.dto.ProductoRequest;
import com.utp.semana4_api_rest.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
        // datos iniciales si la tabla está vacía
        if (repository.count() == 0) {
            repository.save(new Producto(null, "Laptop Lenovo", "Tecnologia", 3500.0, 10));
            repository.save(new Producto(null, "Mouse Logitech", "Tecnologia", 80.0, 25));
            repository.save(new Producto(null, "Silla ergonomica", "Muebles", 750.0, 5));
            // productos adicionales de ejemplo
            repository.save(new Producto(null, "Monitor Samsung", "Tecnologia", 1200.0, 8));
            repository.save(new Producto(null, "Mesa de oficina", "Muebles", 400.0, 12));
        }
    }


    public List<Producto> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return repository.findAll();
        }
        return repository.findByCategoriaIgnoreCase(categoria);
    }

    public Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    public Producto crear(ProductoRequest req) {
        Producto p = new Producto(null, req.getNombre(), req.getCategoria(), req.getPrecio(), req.getStock());
        return repository.save(p);
    }

    public Producto actualizar(Long id, ProductoRequest req) {
        Producto existing = buscarPorId(id);
        existing.setNombre(req.getNombre());
        existing.setCategoria(req.getCategoria());
        existing.setPrecio(req.getPrecio());
        existing.setStock(req.getStock());
        return repository.save(existing);
    }

    public Producto actualizarStock(Long id, Integer stock) {
        Producto existing = buscarPorId(id);
        existing.setStock(stock);
        return repository.save(existing);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductoNoEncontradoException(id);
        }
        repository.deleteById(id);
    }
}
