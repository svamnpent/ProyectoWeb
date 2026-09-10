package com.phantom.api.service;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.UsuarioRequestDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioDTO> obtenerTodas();

    UsuarioDTO obtenerPorId(Long id);

    UsuarioDTO crear(UsuarioRequestDTO request);

    UsuarioDTO actualizar(Long id, UsuarioRequestDTO request);

    void eliminar(Long id);

    UsuarioDTO buscarPorEmail(String email);
}
