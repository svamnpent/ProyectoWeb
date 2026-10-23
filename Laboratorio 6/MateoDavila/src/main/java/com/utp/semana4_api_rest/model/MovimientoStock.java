package com.utp.semana4_api_rest.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(nullable = false, length = 20)
    private String tipo;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(nullable = false)
    private LocalDateTime fecha;

    public MovimientoStock() {
    }

    public MovimientoStock(Producto producto, String tipo, Integer cantidad, LocalDateTime fecha) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}