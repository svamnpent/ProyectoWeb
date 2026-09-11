package com.phantom.api.service.impl;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.request.UsuarioRequestDTO;
import com.phantom.api.entity.Usuario;
import com.phantom.api.entity.Perfil;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.repository.PerfilRepository;
import com.phantom.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerTodas() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
        return mapToDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO crear(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }
        if (request.getDni() != null && usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + request.getDni());
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(request.getEmail().trim())
                .password(request.getPassword())
                .rol(request.getRol() != null ? request.getRol().trim() : "USER")
                .dni(request.getDni())
                .direccion(request.getDireccion())
                .numeroTelefono(request.getNumeroTelefono())
                .build();
        return mapToDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
        // Email uniqueness check if changed
        if (!usuario.getEmail().equalsIgnoreCase(request.getEmail().trim()) && usuarioRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Ya existe otro usuario con el email: " + request.getEmail());
        }
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(request.getEmail().trim());
        if (request.getPassword() != null) {
            usuario.setPassword(request.getPassword());
        }
        if (request.getRol() != null) {
            usuario.setRol(request.getRol().trim());
        }
        usuario.setDni(request.getDni());
        usuario.setDireccion(request.getDireccion());
        usuario.setNumeroTelefono(request.getNumeroTelefono());
        return mapToDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Usuario no encontrado con el ID: " + id);
        }
        // Remove perfil if exists to avoid FK issues
        perfilRepository.findByUsuarioId(id).ifPresent(perfilRepository::delete);
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + email));
        return mapToDTO(usuario);
    }

    // Mapper manual
    private UsuarioDTO mapToDTO(Usuario usuario) {
        PerfilDTO perfilDTO = null;
        if (usuario.getPerfil() != null) {
            perfilDTO = PerfilDTO.builder()
                    .id(usuario.getPerfil().getId())
                    .avatarUrl(usuario.getPerfil().getAvatarUrl())
                    .biografia(usuario.getPerfil().getBiografia())
                    .usuarioId(usuario.getId())
                    .build();
        }
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .dni(usuario.getDni())
                .direccion(usuario.getDireccion())
                .numeroTelefono(usuario.getNumeroTelefono())
                .fechaRegistro(usuario.getFechaRegistro())
                .perfil(perfilDTO)
                .build();
    }
}
