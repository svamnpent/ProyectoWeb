package com.phantom.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {

    private Long usuarioId; // Opcional (compra anónima o con usuario)

    @NotEmpty(message = "La venta debe incluir al menos un detalle de producto")
    @Valid // Dispara la validación interna de cada ítem de la lista
    private List<DetalleVentaRequestDTO> detalles;
}