package com.phantom.api.service;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;

import java.util.List;

public interface DetalleVentaService {
    List<DetalleVentaDTO> obtenerPorVentaId(Long ventaId);

    DetalleVentaDTO crear(Long ventaId, DetalleVentaRequestDTO request);

    void eliminar(Long id);
}
