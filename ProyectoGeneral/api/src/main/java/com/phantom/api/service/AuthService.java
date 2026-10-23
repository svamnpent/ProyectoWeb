package com.phantom.api.service;

import com.phantom.api.dto.AuthResponseDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.LoginRequestDTO;
import com.phantom.api.dto.request.RegisterRequestDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);

    UsuarioDTO me();
}
