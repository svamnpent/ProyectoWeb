package com.utp.semana4_api_rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.semana4_api_rest.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}