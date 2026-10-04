package com.utp.semana4_api_rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.semana4_api_rest.model.MovimientoStock;

public interface MovimientoStockRepository
        extends JpaRepository<MovimientoStock, Long> {
}