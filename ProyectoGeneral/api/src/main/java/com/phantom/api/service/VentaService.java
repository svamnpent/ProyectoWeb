package com.phantom.api.service;

import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.request.VentaRequestDTO;

import java.util.List;

public interface VentaService {
    List<VentaDTO> obtenerTodas();

    VentaDTO obtenerPorId(Long id);

    VentaDTO crear(VentaRequestDTO request);

    void eliminar(Long id);
}
