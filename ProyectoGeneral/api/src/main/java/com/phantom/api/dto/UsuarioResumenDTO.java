package com.phantom.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos mínimos de un usuario para mostrarlo dentro de otros recursos (p. ej. el cliente de una venta).
 * Intencionalmente SIN DNI, dirección ni teléfono: quien atiende una venta (TRABAJADOR) no necesita
 * los datos personales completos del cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumenDTO {
    private Long id;
    private String nombre;
    private String email;
}
