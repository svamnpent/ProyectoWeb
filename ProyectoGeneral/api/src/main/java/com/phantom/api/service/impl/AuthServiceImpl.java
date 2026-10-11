package com.phantom.api.service.impl;

import com.phantom.api.dto.AuthResponseDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.LoginRequestDTO;
import com.phantom.api.dto.request.RegistroRequestDTO;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.security.JwtService;
import com.phantom.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponseDTO registrar(RegistroRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }
        if (request.getDni() != null && !request.getDni().isBlank() && usuarioRepository.existsByDni(request.getDni().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + request.getDni());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(request.getEmail().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol("USER")
                .dni(request.getDni() != null ? request.getDni().trim() : null)
                .direccion(request.getDireccion())
                .numeroTelefono(request.getNumeroTelefono())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        var userDetails = userDetailsService.loadUserByUsername(guardado.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .tipo("Bearer")
                .usuario(mapToUsuarioDTO(guardado))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + request.getEmail()));

        var userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .tipo("Bearer")
                .usuario(mapToUsuarioDTO(usuario))
                .build();
    }

    private UsuarioDTO mapToUsuarioDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .dni(usuario.getDni())
                .direccion(usuario.getDireccion())
                .numeroTelefono(usuario.getNumeroTelefono())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }
}

