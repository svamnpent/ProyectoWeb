package com.phantom.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRequestDTO {

    @Size(max = 255, message = "La URL del avatar no puede exceder los 255 caracteres")
    private String avatarUrl;

    @Size(max = 255, message = "La biografía no puede exceder los 255 caracteres")
    private String biografia;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;
}