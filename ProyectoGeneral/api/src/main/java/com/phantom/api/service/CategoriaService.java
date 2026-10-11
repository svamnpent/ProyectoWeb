package com.phantom.api.service;

import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.request.CategoriaRequestDTO;

import java.util.List;

public interface CategoriaService {
    List<CategoriaDTO> obtenerTodas();

    CategoriaDTO obtenerPorId(Long id);

    List<CategoriaDTO> buscarPorNombre(String keyword);

    CategoriaDTO crear(CategoriaRequestDTO request);

    CategoriaDTO actualizar(Long id, CategoriaRequestDTO request);

    void eliminar(Long id);
}