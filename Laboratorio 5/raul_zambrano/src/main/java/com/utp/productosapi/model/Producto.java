package com.utp.productosapi.model;

public class Producto {
	private Long id;
	private String nombre;
	private double precio;
	private int stock;
	private String categoria;

	public Producto() {
	}

	public Producto(Long id, String nombre, double precio, int stock) {
		this(id, nombre, precio, stock, null);
	}

	public Producto(Long id, String nombre, double precio, int stock, String categoria) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.stock = stock;
		this.categoria = categoria;
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

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
}
