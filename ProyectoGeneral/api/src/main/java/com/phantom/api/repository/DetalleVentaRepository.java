
package com.phantom.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * @Repository
 * public interface DetalleVentaRepository extends JpaRepository<DetalleVenta,
 * Long> {
 * 
 * List<DetalleVenta> findByVentaId(Long ventaId);
 * 
 * // Consulta analítica útil para dashboard: Top N productos más vendidos por
 * // volumen
 * 
 * @Query("SELECT d.producto.id, d.producto.nombre, SUM(d.cantidad) as totalVendido "
 * +
 * "FROM DetalleVenta d " +
 * "GROUP BY d.producto.id, d.producto.nombre " +
 * "ORDER BY totalVendido DESC")
 * List<Object[]> findProductosMasVendidos();
 * }
 */