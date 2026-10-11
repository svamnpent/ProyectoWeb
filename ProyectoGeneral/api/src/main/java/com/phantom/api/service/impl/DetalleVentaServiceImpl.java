package com.phantom.api.service.impl;

import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.entity.DetalleVenta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Venta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.DetalleVentaRepository;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.VentaRepository;
import com.phantom.api.service.DetalleVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetalleVentaServiceImpl implements DetalleVentaService {

    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleVentaDTO> obtenerPorVentaId(Long ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetalleVentaDTO crear(Long ventaId, DetalleVentaRequestDTO request) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con el ID: " + ventaId));
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + request.getProductoId()));
        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));
        DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .producto(producto)
                .cantidad(request.getCantidad())
                .precioUnitario(precioUnitario)
                .subtotal(subtotal)
                .build();
        return mapToDTO(detalleVentaRepository.save(detalle));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!detalleVentaRepository.existsById(id)) {
            throw new ResourceNotFoundException("DetalleVenta no encontrado con el ID: " + id);
        }
        detalleVentaRepository.deleteById(id);
    }

    private DetalleVentaDTO mapToDTO(DetalleVenta detalle) {
        return DetalleVentaDTO.builder()
                .id(detalle.getId())
                .productoId(detalle.getProducto().getId())
                .productoNombre(detalle.getProducto().getNombre())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build();
    }
}
