package com.phantom.api.service;

import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.request.EtiquetaRequestDTO;

import java.util.List;

public interface EtiquetaService {
    List<EtiquetaDTO> obtenerTodas();

    EtiquetaDTO obtenerPorId(Long id);

    EtiquetaDTO crear(EtiquetaRequestDTO request);

    EtiquetaDTO actualizar(Long id, EtiquetaRequestDTO request);

    void eliminar(Long id);
}
