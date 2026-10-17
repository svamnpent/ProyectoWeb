package com.phantom.api.service.impl;

import com.phantom.api.config.CacheNames;
import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.dto.request.ProductoRequestDTO;
import com.phantom.api.entity.Categoria;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.entity.Producto;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.CategoriaRepository;
import com.phantom.api.repository.EtiquetaRepository;
import com.phantom.api.repository.ProductoRepository;
import com.phantom.api.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
        @Cacheable(cacheNames = CacheNames.PRODUCTOS, key = "'todas'")
        public List<ProductoDTO> obtenerTodas() {
                return productoRepository.findAllConCategoriasYEtiquetas().stream()
                                .map(this::mapToDTO)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(cacheNames = CacheNames.PRODUCTOS, key = "'id:' + #id")
        public ProductoDTO obtenerPorId(Long id) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Producto no encontrado con el ID: " + id));
                return mapToDTO(producto);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(cacheNames = CacheNames.PRODUCTOS, key = "'kw:' + #keyword.toLowerCase()")
        public List<ProductoDTO> buscarPorPalabraClave(String keyword) {
                // La búsqueda se resuelve en la BD (antes se cargaba todo el catálogo y se filtraba en memoria)
                return productoRepository.buscarConCategoriaYEtiquetas(keyword).stream()
                                .map(this::mapToDTO)
                                .toList();
        }

        @Override
        @Transactional
        @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true)
        public ProductoDTO crear(ProductoRequestDTO request) {
                Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Categoría no encontrada con el ID: " + request.getCategoriaId()));

                Producto producto = Producto.builder()
                                .nombre(request.getNombre().trim())
                                .descripcion(request.getDescripcion())
                                .precio(request.getPrecio())
                                .stock(request.getStock())
                                .marca(request.getMarca())
                                .imagenUrl(request.getImagenUrl())
                                .categoria(categoria)
                                .etiquetas(resolverEtiquetas(request.getEtiquetaIds()))
                                .build();

                return mapToDTO(productoRepository.save(producto));
        }

        @Override
        @Transactional
        @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true)
        public ProductoDTO actualizar(Long id, ProductoRequestDTO request) {
                Producto producto = productoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Producto no encontrado con el ID: " + id));

                if (!producto.getCategoria().getId().equals(request.getCategoriaId())) {
                        Categoria nuevaCategoria = categoriaRepository.findById(request.getCategoriaId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Categoría no encontrada con el ID: "
                                                                        + request.getCategoriaId()));
                        producto.setCategoria(nuevaCategoria);
                }

                // null = no tocar las etiquetas; conjunto vacío = quitarlas todas
                if (request.getEtiquetaIds() != null) {
                        producto.setEtiquetas(resolverEtiquetas(request.getEtiquetaIds()));
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
        @CacheEvict(cacheNames = CacheNames.PRODUCTOS, allEntries = true)
        public void eliminar(Long id) {
                if (!productoRepository.existsById(id)) {
                        throw new ResourceNotFoundException(
                                        "No se puede eliminar. Producto no encontrado con el ID: " + id);
                }
                productoRepository.deleteById(id);
        }

        /**
         * Resuelve los IDs de etiquetas a entidades (conjunto mutable). Si alguno no existe falla,
         * en lugar de ignorarlo en silencio como antes.
         */
        private Set<Etiqueta> resolverEtiquetas(Set<Long> ids) {
                if (ids == null || ids.isEmpty()) {
                        return new HashSet<>();
                }
                List<Etiqueta> encontradas = etiquetaRepository.findByIdIn(ids);
                if (encontradas.size() != ids.size()) {
                        Set<Long> faltantes = new HashSet<>(ids);
                        encontradas.forEach(e -> faltantes.remove(e.getId()));
                        throw new ResourceNotFoundException("Etiquetas no encontradas con los IDs: " + faltantes);
                }
                return new HashSet<>(encontradas);
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
