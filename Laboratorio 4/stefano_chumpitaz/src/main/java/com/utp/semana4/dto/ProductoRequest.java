package com.utp.semana4.dto;

 
import jakarta.validation.constraints.Min; 
import jakarta.validation.constraints.NotBlank; 
import jakarta.validation.constraints.NotNull; 
import jakarta.validation.constraints.Positive;

public class ProductoRequest {
     @NotBlank(message = "El nombre es obligatorio") 
    private String nombre; 
 
    @NotBlank(message = "La categoria es obligatoria") 
    private String categoria; 
 
    @NotNull(message = "El precio es obligatorio") 
    @Positive(message = "El precio debe ser mayor a cero") 
    private Double precio; 
@NotNull(message = "El stock es obligatorio") 
    @Min(value = 0, message = "El stock no puede ser negativo") 
    private Integer stock; 
 
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
 
    public Double getPrecio() { 
        return precio; 
    } 
 
    public void setPrecio(Double precio) { 
        this.precio = precio; 
    } 
 
    public Integer getStock() { 
        return stock; 
    } 
 
    public void setStock(Integer stock) { 
        this.stock = stock; 
    } 
}
