package com.phantom.api.service.impl;

import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.request.PerfilRequestDTO;
import com.phantom.api.entity.Perfil;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.PerfilRepository;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.service.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PerfilDTO> obtenerTodos() {
        return perfilRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilDTO obtenerPorId(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con el ID: " + id));
        return mapToDTO(perfil);
    }

    @Override
    @Transactional
    public PerfilDTO crear(PerfilRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + request.getUsuarioId()));
        // Ensure user doesn't already have a profile
        perfilRepository.findByUsuarioId(request.getUsuarioId()).ifPresent(p -> {
            throw new IllegalArgumentException("El usuario ya tiene un perfil asignado");
        });
        Perfil perfil = Perfil.builder()
                .avatarUrl(request.getAvatarUrl())
                .biografia(request.getBiografia())
                .usuario(usuario)
                .build();
        return mapToDTO(perfilRepository.save(perfil));
    }

    @Override
    @Transactional
    public PerfilDTO actualizar(Long id, PerfilRequestDTO request) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con el ID: " + id));
        // If userId changes, verify existence and uniqueness
        if (!perfil.getUsuario().getId().equals(request.getUsuarioId())) {
            Usuario nuevoUsuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + request.getUsuarioId()));
            // Ensure new user has no profile
            perfilRepository.findByUsuarioId(request.getUsuarioId()).ifPresent(p -> {
                throw new IllegalArgumentException("El nuevo usuario ya tiene un perfil asignado");
            });
            perfil.setUsuario(nuevoUsuario);
        }
        perfil.setAvatarUrl(request.getAvatarUrl());
        perfil.setBiografia(request.getBiografia());
        return mapToDTO(perfilRepository.save(perfil));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!perfilRepository.existsById(id)) {
            throw new ResourceNotFoundException("Perfil no encontrado con el ID: " + id);
        }
        perfilRepository.deleteById(id);
    }

    private PerfilDTO mapToDTO(Perfil perfil) {
        return PerfilDTO.builder()
                .id(perfil.getId())
                .avatarUrl(perfil.getAvatarUrl())
                .biografia(perfil.getBiografia())
                .usuarioId(perfil.getUsuario().getId())
                .build();
    }
}
