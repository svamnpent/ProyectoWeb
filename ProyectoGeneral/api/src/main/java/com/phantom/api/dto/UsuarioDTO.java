package com.phantom.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private String dni;
    private String direccion;
    private String numeroTelefono;
    private OffsetDateTime fechaRegistro;
    private PerfilDTO perfil;
}