package com.utp.semana4.model;

public class Producto {
    private Long id; 
    private String nombre; 
    private String categoria; 
    private double precio; 
    private int stock; 
 
    public Producto() { 
    } 
 
    public Producto(Long id, String nombre, String categoria, double precio, int stock) { 
        this.id = id; 
        this.nombre = nombre; 
        this.categoria = categoria; 
        this.precio = precio; 
        this.stock = stock; 
    } 
 
    public Long getId() { 

         return id; 
    } 
 
    public void setId(Long id) { 
        this.id = id; 
    } 
 
    public String getNombre() { 
        return nombre; 
    } 
 
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    } 
 
    public String getCategoria() { 
        return categoria; 
    } 
 
    public void setCategoria(String categoria) { 
        this.categoria = categoria; 
    } 
 
    public double getPrecio() { 
        return precio; 
    } 
 
    public void setPrecio(double precio) { 
        this.precio = precio; 
    } 
 
    public int getStock() { 
        return stock; 
    } 
 
    public void setStock(int stock) { 
        this.stock = stock; 
    } 
} 

