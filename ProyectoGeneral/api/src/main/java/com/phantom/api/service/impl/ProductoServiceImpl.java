package com.phantom.api.service.impl;

import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.request.ProductoRequestDTO;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.entity.Producto;
import com.phantom.api.entity.Categoria;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.repository.CategoriaRepository;
import com.phantom.api.repository.EtiquetaRepository;
import com.phantom.api.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final EtiquetaRepository etiquetaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerTodas() {
        return productoRepository.findAllConCategoriasYEtiquetas().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));
        return mapToDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorPalabraClave(String keyword) {
        // Simple implementation using repository's buscarPorPalabraClave with unpaged request
        // For brevity, we reuse findAllConCategoriasYEtiquetas and filter in memory
        return productoRepository.findAllConCategoriasYEtiquetas().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(keyword.toLowerCase())
                        || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(keyword.toLowerCase()))
                        || (p.getMarca() != null && p.getMarca().toLowerCase().contains(keyword.toLowerCase())))
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProductoDTO crear(ProductoRequestDTO request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + request.getCategoriaId()));

        Set<Etiqueta> etiquetas = request.getEtiquetaIds() == null ? Set.of() :
                etiquetaRepository.findByIdIn(new java.util.ArrayList<>(request.getEtiquetaIds())).stream().collect(Collectors.toSet());

        Producto producto = Producto.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .marca(request.getMarca())
                .imagenUrl(request.getImagenUrl())
                .categoria(categoria)
                .etiquetas(etiquetas)
                .build();

        return mapToDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoDTO actualizar(Long id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));

        if (!producto.getCategoria().getId().equals(request.getCategoriaId())) {
            Categoria nuevaCategoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + request.getCategoriaId()));
            producto.setCategoria(nuevaCategoria);
        }

        if (request.getEtiquetaIds() != null) {
Set<Etiqueta> nuevasEtiquetas = request.getEtiquetaIds() == null ? Set.of() :
                etiquetaRepository.findByIdIn(new java.util.ArrayList<>(request.getEtiquetaIds())).stream()
                        .collect(Collectors.toSet());
            producto.setEtiquetas(nuevasEtiquetas);
        }

        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setMarca(request.getMarca());
        producto.setImagenUrl(request.getImagenUrl());

        return mapToDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Producto no encontrado con el ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    // Mapper manual
    private ProductoDTO mapToDTO(Producto producto) {
        Set<EtiquetaDTO> etiquetaDTOs = producto.getEtiquetas().stream()
                .map(e -> EtiquetaDTO.builder()
                        .id(e.getId())
                        .nombre(e.getNombre())
                        .build())
                .collect(Collectors.toSet());
        CategoriaDTO categoriaDTO = CategoriaDTO.builder()
                .id(producto.getCategoria().getId())
                .nombre(producto.getCategoria().getNombre())
                .descripcion(producto.getCategoria().getDescripcion())
                .build();
        return ProductoDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .marca(producto.getMarca())
                .imagenUrl(producto.getImagenUrl())
                .categoria(categoriaDTO)
                .etiquetas(etiquetaDTOs)
                .build();
    }
}
