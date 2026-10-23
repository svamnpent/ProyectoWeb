package com.phantom.api.mapper;

import com.phantom.api.config.AppTime;
import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.UsuarioResumenDTO;
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

    /** Datos mínimos (id, nombre, email) para mostrar el usuario dentro de otros recursos, p. ej. una venta. */
    public static UsuarioResumenDTO toResumen(Usuario usuario) {
        return UsuarioResumenDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
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
                .fechaRegistro(AppTime.aLima(usuario.getFechaRegistro()));
    }
}
