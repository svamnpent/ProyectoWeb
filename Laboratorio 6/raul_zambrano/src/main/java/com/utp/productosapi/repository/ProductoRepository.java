package com.utp.productosapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.productosapi.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
