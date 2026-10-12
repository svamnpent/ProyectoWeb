package com.phantom.api.mapper;

import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.entity.Usuario;

/**
 * Mapeo Usuario -> UsuarioDTO. Nunca expone el password.
 */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioDTO toDTO(Usuario usuario) {
        PerfilDTO perfilDTO = null;
        if (usuario.getPerfil() != null) {
            perfilDTO = PerfilDTO.builder()
                    .id(usuario.getPerfil().getId())
                    .avatarUrl(usuario.getPerfil().getAvatarUrl())
                    .biografia(usuario.getPerfil().getBiografia())
                    .usuarioId(usuario.getId())
                    .build();
        }
        return base(usuario).perfil(perfilDTO).build();
    }

    /** Versión sin perfil (evita cargar la relación 1-1 inversa, p. ej. al listar ventas). */
    public static UsuarioDTO toDTOSinPerfil(Usuario usuario) {
        return base(usuario).build();
    }

    private static UsuarioDTO.UsuarioDTOBuilder base(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .dni(usuario.getDni())
                .direccion(usuario.getDireccion())
                .numeroTelefono(usuario.getNumeroTelefono())
                .fechaRegistro(usuario.getFechaRegistro());
    }
}
