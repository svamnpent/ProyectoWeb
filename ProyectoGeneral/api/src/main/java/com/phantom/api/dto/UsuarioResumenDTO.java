package com.phantom.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Vista del cliente que omite DNI, dirección y teléfono. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumenDTO {
    private Long id;
    private String nombre;
    private String email;
}
