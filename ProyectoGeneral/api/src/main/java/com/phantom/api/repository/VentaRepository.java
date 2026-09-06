package com.phantom.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/*
 * @Repository
 * public interface VentaRepository extends JpaRepository<Venta, Long> {
 * 
 * // Historial de compras paginado para el usuario activo
 * Page<Venta> findByUsuarioOrderByFechaDesc(Usuario usuario, Pageable
 * pageable);
 * 
 * Page<Venta> findByUsuarioIdOrderByFechaDesc(Long usuarioId, Pageable
 * pageable);
 * 
 * Optional<Venta> findByNumeroOrden(String numeroOrden);
 * 
 * // Cargar la venta con sus detalles y cliente de una sola consulta
 * (Optimización
 * // JOIN FETCH)
 * 
 * @Query("SELECT DISTINCT v FROM Venta v " +
 * "LEFT JOIN FETCH v.usuario u " +
 * "LEFT JOIN FETCH v.detalles d " +
 * "LEFT JOIN FETCH d.producto " +
 * "WHERE v.id = :id")
 * Optional<Venta> findByIdConDetallesYProducto(@Param("id") Long id);
 * 
 * // Búsqueda general para Panel Admin
 * 
 * @Query("SELECT v FROM Venta v LEFT JOIN v.usuario u WHERE " +
 * "CAST(v.id AS string) LIKE %:keyword% OR " +
 * "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
 * "LOWER(v.numeroOrden) LIKE LOWER(CONCAT('%', :keyword, '%'))")
 * Page<Venta> findByKeyword(@Param("keyword") String keyword, Pageable
 * pageable);
 * 
 * // Reportes de ventas entre un rango de fechas
 * Page<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin,
 * Pageable pageable);
 * }
 */