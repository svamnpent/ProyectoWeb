package com.phantom.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.phantom.api.entity.Producto;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

        // Carga ansiosa (JOIN FETCH) para evitar el problema N+1 al listar catálogo

        @Query("SELECT DISTINCT p FROM Producto p JOIN FETCH p.categoria LEFT JOIN FETCH p.etiquetas")
        List<Producto> findAllConCategoriasYEtiquetas();

        // Filtros típicos para la tienda online (Catálogo paginado)
        Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

        Page<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);

        // Filtrar por stock disponible (productos que se pueden comprar)
        Page<Producto> findByStockGreaterThan(Integer stock, Pageable pageable);

        // Buscar por etiqueta / Tag (relación Muchos a Muchos)
        Page<Producto> findByEtiquetasNombreIgnoreCase(String nombreEtiqueta,
                        Pageable pageable);

        // Buscador general con categoría y etiquetas cargadas (sin N+1), usado por la API
        @Query("SELECT DISTINCT p FROM Producto p JOIN FETCH p.categoria LEFT JOIN FETCH p.etiquetas WHERE " +
                        "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.marca) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Producto> buscarConCategoriaYEtiquetas(@Param("keyword") String keyword);

        // Descuento de stock ATÓMICO a nivel de BD: solo actualiza si hay stock suficiente.
        // Devuelve 1 si descontó, 0 si no había stock (evita sobreventa bajo concurrencia).
        @Modifying
        @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad " +
                        "WHERE p.id = :id AND p.stock >= :cantidad")
        int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

        // Reposición atómica de stock (anulación de ventas o líneas de venta)
        @Modifying
        @Query("UPDATE Producto p SET p.stock = p.stock + :cantidad WHERE p.id = :id")
        int reponerStock(@Param("id") Long id, @Param("cantidad") int cantidad);

        // Buscador general de productos (nombre, descripción o marca)

        @Query("SELECT p FROM Producto p WHERE " +
                        "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.marca) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Producto> buscarPorPalabraClave(@Param("keyword") String keyword,
                        Pageable pageable);
}