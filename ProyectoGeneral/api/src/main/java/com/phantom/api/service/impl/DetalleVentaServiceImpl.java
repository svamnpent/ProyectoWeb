package com.phantom.api.service.impl;

import com.phantom.api.config.CacheNames;
import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.entity.DetalleVenta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Venta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.StockInsuficienteException;
import com.phantom.api.repository.DetalleVentaRepository;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.VentaRepository;
import com.phantom.api.security.CurrentUser;
import com.phantom.api.service.DetalleVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
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
    private final CurrentUser currentUser;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleVentaDTO> obtenerPorVentaId(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con el ID: " + ventaId));
        verificarAcceso(venta);
        return detalleVentaRepository.findByVentaId(ventaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Agrega una línea a una venta existente (operación administrativa). Mantiene coherentes
     * el stock y los totales de la venta, que antes quedaban desactualizados.
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true)
    public DetalleVentaDTO crear(Long ventaId, DetalleVentaRequestDTO request) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con el ID: " + ventaId));
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + request.getProductoId()));

        if (productoRepository.descontarStock(producto.getId(), request.getCantidad()) == 0) {
            throw new StockInsuficienteException(String.format(
                    "Stock insuficiente para '%s': solicitado %d, disponible %d",
                    producto.getNombre(), request.getCantidad(), producto.getStock()));
        }

        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));
        DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .producto(producto)
                .cantidad(request.getCantidad())
                .precioUnitario(precioUnitario)
                .subtotal(subtotal)
                .build();

        venta.setTotal(venta.getTotal().add(subtotal));
        venta.setCantidadItems(venta.getCantidadItems() + request.getCantidad());
        return mapToDTO(detalleVentaRepository.save(detalle));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true)
    public void eliminar(Long id) {
        DetalleVenta detalle = detalleVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DetalleVenta no encontrado con el ID: " + id));

        productoRepository.reponerStock(detalle.getProducto().getId(), detalle.getCantidad());

        Venta venta = detalle.getVenta();
        venta.setTotal(venta.getTotal().subtract(detalle.getSubtotal()));
        venta.setCantidadItems(venta.getCantidadItems() - detalle.getCantidad());

        detalleVentaRepository.delete(detalle);
    }

    private void verificarAcceso(Venta venta) {
        Long duenoId = venta.getUsuario() != null ? venta.getUsuario().getId() : null;
        if (duenoId == null) {
            if (!currentUser.isAdmin()) {
                throw new AccessDeniedException("No tienes permiso para acceder a esta venta");
            }
            return;
        }
        currentUser.requireSelfOrAdmin(duenoId);
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
