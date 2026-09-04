package com.utp.semana4.controller;

import java.net.URI; 
import java.util.List; 

import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.DeleteMapping; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PatchMapping; 
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.PutMapping; 
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.bind.annotation.RestController; 
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.utp.semana4.dto.ActualizarStockRequest; 
import com.utp.semana4.dto.ProductoRequest; 
import com.utp.semana4.model.Producto; 
import com.utp.semana4.service.ProductoService;

import jakarta.validation.Valid;


 
@RestController 
@RequestMapping("/api/productos") 
public class ProductoController {
    private final ProductoService service; 
 
    public ProductoController(ProductoService service) { 
        this.service = service; 
    } 
 
    @GetMapping 
    public ResponseEntity<List<Producto>> listar( 
            @RequestParam(required = false) String categoria) { 
        return ResponseEntity.ok(service.listar(categoria)); 
    } 
 
    @GetMapping("/{id}") 
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) { 
        return ResponseEntity.ok(service.buscarPorId(id)); 
    } 
 
    @PostMapping 
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoRequest request) { 
        Producto nuevoProducto = service.crear(request); 
 
        URI location = ServletUriComponentsBuilder 
                .fromCurrentRequest() 
                .path("/{id}") 
                .buildAndExpand(nuevoProducto.getId()) 
                .toUri(); 
 
        return ResponseEntity.created(location).body(nuevoProducto); 
    } 
 
    @PutMapping("/{id}") 
    public ResponseEntity<Producto> actualizar( 
            @PathVariable Long id, 
            @Valid @RequestBody ProductoRequest request) { 
        return ResponseEntity.ok(service.actualizar(id, request)); 
    } 
 
    @PatchMapping("/{id}/stock") 
    public ResponseEntity<Producto> actualizarStock( 
            @PathVariable Long id, 
            @Valid @RequestBody ActualizarStockRequest request) { 
        return ResponseEntity.ok(service.actualizarStock(id, request)); 
    } 
 
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { 
        service.eliminar(id); 
        return ResponseEntity.noContent().build(); 
    } 
}
