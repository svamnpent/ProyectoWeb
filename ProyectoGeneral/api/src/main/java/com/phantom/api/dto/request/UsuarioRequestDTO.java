package com.phantom.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    // Opcional (por defecto USER). Solo un ADMIN puede asignar/cambiar roles.
    @Pattern(regexp = "^(?i)(USER|ADMIN)$", message = "El rol debe ser USER o ADMIN")
    private String rol;

    @Pattern(regexp = "^[0-9]{8,20}$", message = "El DNI debe contener entre 8 y 20 dígitos numéricos")
    private String dni;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    private String direccion;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El número de teléfono tiene un formato inválido")
    private String numeroTelefono;
}