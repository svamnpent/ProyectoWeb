package com.phantom.api.service.impl;

import com.phantom.api.config.AppTime;
import com.phantom.api.config.CacheNames;
import com.phantom.api.dto.DetalleVentaDTO;
import com.phantom.api.dto.VentaDTO;
import com.phantom.api.dto.request.DetalleVentaRequestDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.entity.DetalleVenta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Usuario;
import com.phantom.api.entity.Venta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.StockInsuficienteException;
import com.phantom.api.mapper.UsuarioMapper;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.repository.VentaRepository;
import com.phantom.api.security.CurrentUser;
import com.phantom.api.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

        private final VentaRepository ventaRepository;
        private final UsuarioRepository usuarioRepository;
        private final ProductoRepository productoRepository;
        private final CurrentUser currentUser;

        @Override
        @Transactional(readOnly = true)
        public List<VentaDTO> obtenerTodas() {
                return ventaRepository.findAll().stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<VentaDTO> obtenerMisVentas() {
                return ventaRepository.findByUsuarioIdOrderByFechaDesc(currentUser.getId(), Pageable.unpaged())
                                .stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public VentaDTO obtenerPorId(Long id) {
                Venta venta = ventaRepository.findByIdConDetallesYProducto(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Venta no encontrada con el ID: " + id));
                verificarAcceso(venta);
                return mapToDTO(venta);
        }

        @Override
        @Transactional
        @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true) // el stock cambia
        public VentaDTO crear(VentaRequestDTO request) {
                Usuario comprador = resolverComprador(request);

                // Unifica líneas repetidas del mismo producto y ordena por ID (orden estable de
                // actualización = sin deadlocks entre compras concurrentes)
                Map<Long, Integer> cantidades = request.getDetalles().stream()
                                .collect(Collectors.toMap(
                                                DetalleVentaRequestDTO::getProductoId,
                                                DetalleVentaRequestDTO::getCantidad,
                                                Integer::sum,
                                                TreeMap::new));

                Map<Long, Producto> productos = productoRepository.findAllById(cantidades.keySet()).stream()
                                .collect(Collectors.toMap(Producto::getId, Function.identity()));
                for (Long productoId : cantidades.keySet()) {
                        if (!productos.containsKey(productoId)) {
                                throw new ResourceNotFoundException("Producto no encontrado con el ID: " + productoId);
                        }
                }

                Venta venta = Venta.builder()
                                .usuario(comprador)
                                .numeroOrden(generarNumeroOrden())
                                .fecha(Instant.now())
                                .build();

                BigDecimal total = BigDecimal.ZERO;
                int cantidadItems = 0;
                List<String> errores = new ArrayList<>();

                for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
                        Producto producto = productos.get(linea.getKey());
                        int cantidad = linea.getValue();

                        // Descuento atómico en BD: solo ocurre si hay stock suficiente
                        if (productoRepository.descontarStock(producto.getId(), cantidad) == 0) {
                                errores.add(String.format(
                                                "Stock insuficiente para '%s': solicitado %d, disponible %d",
                                                producto.getNombre(), cantidad, producto.getStock()));
                                continue;
                        }

                        BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
                        venta.getDetalles().add(DetalleVenta.builder()
                                        .venta(venta)
                                        .producto(producto)
                                        .cantidad(cantidad)
                                        .precioUnitario(producto.getPrecio())
                                        .subtotal(subtotal)
                                        .build());
                        total = total.add(subtotal);
                        cantidadItems += cantidad;
                }

                // Si algún producto no tenía stock se lanza la excepción y la transacción se revierte
                // completa (incluidos los descuentos de los productos que sí tenían stock)
                if (!errores.isEmpty()) {
                        throw new StockInsuficienteException(errores);
                }

                venta.setTotal(total);
                venta.setCantidadItems(cantidadItems);
                return mapToDTO(ventaRepository.save(venta)); // detalles se guardan en cascada
        }

        @Override
        @Transactional
        @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true) // el stock cambia
        public void eliminar(Long id) {
                Venta venta = ventaRepository.findByIdConDetallesYProducto(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Venta no encontrada con el ID: " + id));

                // Repone el stock (suma por producto, en orden estable)
                Map<Long, Integer> aReponer = venta.getDetalles().stream()
                                .collect(Collectors.toMap(
                                                d -> d.getProducto().getId(),
                                                DetalleVenta::getCantidad,
                                                Integer::sum,
                                                TreeMap::new));
                aReponer.forEach(productoRepository::reponerStock);

                ventaRepository.delete(venta);
        }

        /**
         * Un comprador siempre compra a su nombre. Solo el personal de la tienda (ADMIN o TRABAJADOR)
         * puede registrar una venta de mostrador para otro usuario (usuarioId) o sin usuario.
         */
        private Usuario resolverComprador(VentaRequestDTO request) {
                Long usuarioId;
                if (currentUser.isStaff()) {
                        usuarioId = request.getUsuarioId();
                } else {
                        usuarioId = currentUser.getId();
                }
                if (usuarioId == null) {
                        return null;
                }
                return usuarioRepository.findById(usuarioId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado con el ID: " + usuarioId));
        }

        private void verificarAcceso(Venta venta) {
                Long duenoId = venta.getUsuario() != null ? venta.getUsuario().getId() : null;
                if (duenoId == null) {
                        // Venta sin usuario (anónima): solo visible para administradores
                        if (!currentUser.isStaff()) {
                                throw new AccessDeniedException("No tienes permiso para acceder a esta venta");
                        }
                        return;
                }
                currentUser.requireSelfOrStaff(duenoId);
        }

        private String generarNumeroOrden() {
                return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
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
                                .fecha(AppTime.aLima(venta.getFecha()))
                                .total(venta.getTotal())
                                .cantidadItems(venta.getCantidadItems())
                                .numeroOrden(venta.getNumeroOrden())
                                .usuario(venta.getUsuario() != null
                                                ? UsuarioMapper.toResumen(venta.getUsuario())
                                                : null)
                                .detalles(detalleDTOs)
                                .build();
        }
}
