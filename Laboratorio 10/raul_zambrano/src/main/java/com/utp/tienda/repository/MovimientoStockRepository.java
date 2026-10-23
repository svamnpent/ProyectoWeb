package com.utp.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.tienda.model.MovimientoStock;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
}
