package com.phantom.api.service;

import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.request.VentaRequestDTO;

import java.util.List;

public interface VentaService {
    /** Todas las ventas (uso administrativo). */
    List<VentaDTO> obtenerTodas();

    /** Historial de compras del usuario autenticado. */
    List<VentaDTO> obtenerMisVentas();

    /** Solo el dueño de la venta o un ADMIN. */
    VentaDTO obtenerPorId(Long id);

    VentaDTO crear(VentaRequestDTO request);

    /** Elimina la venta y repone el stock de sus productos. */
    void eliminar(Long id);
}
