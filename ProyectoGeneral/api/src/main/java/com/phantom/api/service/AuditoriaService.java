package com.phantom.api.service;

import com.phantom.api.config.AppTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private final JdbcTemplate jdbc;

    public record Movimiento(long id, Instant fecha, String tipo, String referencia, long recursoId,
                             Long clienteId, int unidades, BigDecimal importe, Integer stockFinal) {}
    public record Existencia(long id, String nombre, String categoria, int stock) {}
    public record Informe(LocalDate desde, LocalDate hasta, Instant consulta,
                          List<Movimiento> movimientos, List<Existencia> inventario) {}

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Informe consultar(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde) || hasta.isAfter(desde.plusDays(92))) {
            throw new IllegalArgumentException("Elige un período de entre 1 y 93 días.");
        }
        var consulta = Instant.now();
        var movimientos = jdbc.query("""
                SELECT id,fecha,tipo,referencia,recurso_id,cliente_id,unidades,importe,stock_final
                FROM auditoria_comercial WHERE fecha >= ? AND fecha < ?
                ORDER BY fecha DESC,id DESC
                """, (rs, i) -> new Movimiento(rs.getLong("id"), rs.getTimestamp("fecha").toInstant(),
                rs.getString("tipo"), rs.getString("referencia"), rs.getLong("recurso_id"),
                rs.getObject("cliente_id", Long.class), rs.getInt("unidades"), rs.getBigDecimal("importe"),
                rs.getObject("stock_final", Integer.class)),
                Timestamp.from(desde.atStartOfDay(AppTime.ZONA).toInstant()),
                Timestamp.from(hasta.plusDays(1).atStartOfDay(AppTime.ZONA).toInstant()));
        var inventario = jdbc.query("""
                SELECT p.id,p.nombre,c.nombre AS categoria,p.stock FROM productos p
                JOIN categorias c ON c.id=p.categoria_id ORDER BY p.stock,p.nombre,p.id
                """, (rs, i) -> new Existencia(rs.getLong("id"), rs.getString("nombre"),
                rs.getString("categoria"), rs.getInt("stock")));
        return new Informe(desde, hasta, consulta, movimientos, inventario);
    }
}
