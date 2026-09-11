package com.phantom.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaRequestDTO {

    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;
}