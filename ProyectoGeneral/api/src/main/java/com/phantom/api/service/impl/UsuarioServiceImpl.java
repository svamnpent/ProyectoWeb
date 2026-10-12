package com.phantom.api.service.impl;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.UsuarioRequestDTO;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.mapper.UsuarioMapper;
import com.phantom.api.repository.PerfilRepository;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.security.CurrentUser;
import com.phantom.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final Set<String> ROLES_VALIDOS = Set.of("USER", "ADMIN");

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerTodas() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
        return UsuarioMapper.toDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO crear(UsuarioRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        }
        if (request.getDni() != null && usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + request.getDni());
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(normalizarRol(request.getRol()))
                .dni(request.getDni())
                .direccion(request.getDireccion())
                .numeroTelefono(request.getNumeroTelefono())
                .build();
        return UsuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));

        String email = request.getEmail().trim().toLowerCase();
        if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe otro usuario con el email: " + email);
        }
        if (request.getDni() != null && !request.getDni().equals(usuario.getDni())
                && usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe otro usuario con el DNI: " + request.getDni());
        }

        // Solo un ADMIN puede cambiar roles (evita escalada de privilegios)
        if (request.getRol() != null) {
            String nuevoRol = normalizarRol(request.getRol());
            if (!nuevoRol.equals(usuario.getRol())) {
                if (!currentUser.isAdmin()) {
                    throw new AccessDeniedException("Solo un administrador puede cambiar roles");
                }
                usuario.setRol(nuevoRol);
            }
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(email);
        if (request.getPassword() != null) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        usuario.setDni(request.getDni());
        usuario.setDireccion(request.getDireccion());
        usuario.setNumeroTelefono(request.getNumeroTelefono());
        return UsuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Usuario no encontrado con el ID: " + id);
        }
        // Elimina el perfil primero (si existe) para evitar problemas de FK
        perfilRepository.findByUsuarioId(id).ifPresent(perfilRepository::delete);
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + email));
        return UsuarioMapper.toDTO(usuario);
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "USER";
        }
        String normalizado = rol.trim().toUpperCase();
        if (!ROLES_VALIDOS.contains(normalizado)) {
            throw new IllegalArgumentException("Rol inválido: " + rol + ". Valores permitidos: USER, ADMIN");
        }
        return normalizado;
    }
}
