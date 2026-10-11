package com.phantom.api.service.impl;

import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.entity.Venta;
import com.phantom.api.entity.DetalleVenta;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.StockInsuficienteException;
import com.phantom.api.repository.VentaRepository;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.repository.DetalleVentaRepository;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

        private final VentaRepository ventaRepository;
        private final UsuarioRepository usuarioRepository;
        private final DetalleVentaRepository detalleVentaRepository;
        private final ProductoRepository productoRepository;

        @Override
        @Transactional(readOnly = true)
        public List<VentaDTO> obtenerTodas() {
                return ventaRepository.findAll().stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public VentaDTO obtenerPorId(Long id) {
                Venta venta = ventaRepository.findByIdConDetallesYProducto(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Venta no encontrada con el ID: " + id));
                return mapToDTO(venta);
        }

        @Override
        @Transactional
        public VentaDTO crear(VentaRequestDTO request) {
                Usuario usuario = null;
                if (request.getUsuarioId() != null) {
                        usuario = usuarioRepository.findById(request.getUsuarioId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Usuario no encontrado con el ID: " + request.getUsuarioId()));
                }

                // ── Paso 1: Procesar los detalles SIN guardar aún la Venta ──────────────
                // Necesitamos calcular total y cantidadItems antes del primer save
                // para respetar la restricción NOT NULL de la tabla ventas.
                final String numeroOrden = generateOrderNumber();
                final Usuario usuarioFinal = usuario;

                // Validar stock y preparar detalles (sin ID de venta aún)
                List<DetalleVenta> detallesTemp = request.getDetalles().stream()
                                .map(detReq -> {
                                        var producto = productoRepository.findById(detReq.getProductoId())
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Producto no encontrado con el ID: "
                                                                                        + detReq.getProductoId()));
                                        if (producto.getStock() < detReq.getCantidad()) {
                                                throw new StockInsuficienteException(
                                                                "Stock insuficiente para el producto '" + producto.getNombre()
                                                                                + "'. Stock disponible: " + producto.getStock()
                                                                                + ", solicitado: " + detReq.getCantidad());
                                        }
                                        // Descontar stock del producto
                                        producto.setStock(producto.getStock() - detReq.getCantidad());
                                        productoRepository.save(producto);

                                        BigDecimal precioUnitario = producto.getPrecio();
                                        BigDecimal subtotal = precioUnitario
                                                        .multiply(BigDecimal.valueOf(detReq.getCantidad()));
                                        // Guardamos venta=null temporalmente; se asignará después
                                        return DetalleVenta.builder()
                                                        .producto(producto)
                                                        .cantidad(detReq.getCantidad())
                                                        .precioUnitario(precioUnitario)
                                                        .subtotal(subtotal)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // ── Paso 2: Calcular agregados para cumplir restricciones NOT NULL ────────
                BigDecimal total = detallesTemp.stream()
                                .map(DetalleVenta::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                int cantidadItems = detallesTemp.stream()
                                .mapToInt(DetalleVenta::getCantidad)
                                .sum();

                // ── Paso 3: Crear y guardar la Venta con todos los valores ya calculados ──
                Venta venta = Venta.builder()
                                .usuario(usuarioFinal)
                                .numeroOrden(numeroOrden)
                                .total(total)
                                .cantidadItems(cantidadItems)
                                .build();
                venta = ventaRepository.save(venta);

                // ── Paso 4: Asignar la Venta a cada detalle y persistir ───────────────────
                final Venta ventaGuardada = venta;
                List<DetalleVenta> detalles = detallesTemp.stream()
                                .map(d -> {
                                        d.setVenta(ventaGuardada);
                                        return d;
                                })
                                .collect(Collectors.toList());
                detalleVentaRepository.saveAll(detalles);
                venta.setDetalles(detalles);

                return mapToDTO(venta);
        }

        @Override
        @Transactional
        public void eliminar(Long id) {
                Venta venta = ventaRepository.findByIdConDetallesYProducto(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con el ID: " + id));

                // Revertir stock de los productos
                for (DetalleVenta detalle : venta.getDetalles()) {
                        var producto = detalle.getProducto();
                        producto.setStock(producto.getStock() + detalle.getCantidad());
                        productoRepository.save(producto);
                }

                ventaRepository.delete(venta);
        }

        private String generateOrderNumber() {
                // Simple order number generator: UUID short
                return java.util.UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase();
        }

        private VentaDTO mapToDTO(Venta venta) {
                List<DetalleVentaDTO> detalleDTOs = venta.getDetalles().stream()
                                .map(d -> DetalleVentaDTO.builder()
                                                .id(d.getId())
                                                .productoId(d.getProducto().getId())
                                                .productoNombre(d.getProducto().getNombre())
                                                .cantidad(d.getCantidad())
                                                .precioUnitario(d.getPrecioUnitario())
                                                .subtotal(d.getSubtotal())
                                                .build())
                                .collect(Collectors.toList());
                return VentaDTO.builder()
                                .id(venta.getId())
                                .fecha(venta.getFecha())
                                .total(venta.getTotal())
                                .cantidadItems(venta.getCantidadItems())
                                .numeroOrden(venta.getNumeroOrden())
                                .usuario(venta.getUsuario() != null ? com.phantom.api.dto.UsuarioDTO.builder()
                                                .id(venta.getUsuario().getId())
                                                .nombre(venta.getUsuario().getNombre())
                                                .email(venta.getUsuario().getEmail())
                                                .rol(venta.getUsuario().getRol())
                                                .dni(venta.getUsuario().getDni())
                                                .direccion(venta.getUsuario().getDireccion())
                                                .numeroTelefono(venta.getUsuario().getNumeroTelefono())
                                                .fechaRegistro(venta.getUsuario().getFechaRegistro())
                                                .build()
                                                : null)
                                .detalles(detalleDTOs)
                                .build();
        }
}
