package com.utp.tienda.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
