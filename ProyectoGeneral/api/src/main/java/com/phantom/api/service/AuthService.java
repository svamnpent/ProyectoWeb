package com.phantom.api.service;

import com.phantom.api.dto.AuthResponseDTO;
import com.phantom.api.dto.request.LoginRequestDTO;
import com.phantom.api.dto.request.RegistroRequestDTO;

public interface AuthService {
    AuthResponseDTO registrar(RegistroRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}

