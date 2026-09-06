package com.phantom.api.service.impl;

import com.phantom.api.dto.CategoriaDTO;
import com.phantom.api.dto.CategoriaRequestDTO;
import com.phantom.api.entity.Categoria;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.CategoriaRepository;
import com.phantom.api.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + id));
        return mapToDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> buscarPorNombre(String keyword) {
        return categoriaRepository.findByNombreContainingIgnoreCase(keyword).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaDTO crear(CategoriaRequestDTO request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría registrada con el nombre: " + request.getNombre());
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .build();

        return mapToDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaRequestDTO request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + id));

        // Verificar duplicados solo si cambió el nombre
        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre().trim()) &&
                categoriaRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion());

        return mapToDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Categoría no encontrada con el ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    // Mapper manual
    private CategoriaDTO mapToDTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}