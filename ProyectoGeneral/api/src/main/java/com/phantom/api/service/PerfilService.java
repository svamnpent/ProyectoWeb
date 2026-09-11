package com.phantom.api.service;

import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.request.PerfilRequestDTO;

import java.util.List;

public interface PerfilService {
    List<PerfilDTO> obtenerTodos();

    PerfilDTO obtenerPorId(Long id);

    PerfilDTO crear(PerfilRequestDTO request);

    PerfilDTO actualizar(Long id, PerfilRequestDTO request);

    void eliminar(Long id);
}
