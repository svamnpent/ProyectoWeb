package com.utp.semana4.service;

import java.util.Comparator; 
import java.util.List; 
import java.util.Map; 
import java.util.concurrent.ConcurrentHashMap; 
import java.util.concurrent.atomic.AtomicLong;  
import org.springframework.stereotype.Service;

import com.utp.semana4.dto.ActualizarStockRequest; 
import com.utp.semana4.dto.ProductoRequest; 
import com.utp.semana4.exception.ProductoNoEncontradoException; 
import com.utp.semana4.model.Producto;

@Service 
public class ProductoService {
     private final Map<Long, Producto> productos = new ConcurrentHashMap<>(); 
    private final AtomicLong secuencia = new AtomicLong(1); 
 
    public ProductoService() { 
        registrarInicial("Laptop Lenovo", "Tecnologia", 3500.00, 10); 
        registrarInicial("Mouse Logitech", "Tecnologia", 80.00, 25); 
        registrarInicial("Silla ergonomica", "Muebles", 750.00, 5); 
    } 
 
    public List<Producto> listar(String categoria) { 
        return productos.values() 
                .stream() 
                .filter(producto -> categoria == null 
                        || producto.getCategoria().equalsIgnoreCase(categoria)) 
                .sorted(Comparator.comparing(Producto::getId)) 
                .toList(); 
    } 
 
    public Producto buscarPorId(Long id) { 
        Producto producto = productos.get(id); 
 
        if (producto == null) { 
            throw new ProductoNoEncontradoException(id); 
        } 
 
        return producto; 
    } 
 
    public Producto crear(ProductoRequest request) { 
        Long id = secuencia.getAndIncrement(); 
        Producto producto = new Producto( 
                id, 
                request.getNombre(), 
                request.getCategoria(), 
                request.getPrecio(), 
                request.getStock() 
        ); 
        productos.put(id, producto); 
        return producto; 
    } 
 
    public Producto actualizar(Long id, ProductoRequest request) { 
        Producto producto = buscarPorId(id); 
        producto.setNombre(request.getNombre()); 
        producto.setCategoria(request.getCategoria()); 
        producto.setPrecio(request.getPrecio()); 
        producto.setStock(request.getStock()); 
        return producto; 
 } 
 
    public Producto actualizarStock(Long id, ActualizarStockRequest request) { 
        Producto producto = buscarPorId(id); 
        producto.setStock(request.getStock()); 
        return producto; 
    } 
 
    public void eliminar(Long id) { 
        Producto eliminado = productos.remove(id); 
 
        if (eliminado == null) { 
            throw new ProductoNoEncontradoException(id); 
        } 
    } 
 
    private void registrarInicial(String nombre, String categoria, double precio, int stock) { 
        Long id = secuencia.getAndIncrement(); 
        productos.put(id, new Producto(id, nombre, categoria, precio, stock)); 
    } 
}
