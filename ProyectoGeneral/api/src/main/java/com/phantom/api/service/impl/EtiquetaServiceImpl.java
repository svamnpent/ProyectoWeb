package com.phantom.api.service.impl;

import com.phantom.api.dto.EtiquetaDTO;
import com.phantom.api.dto.request.EtiquetaRequestDTO;
import com.phantom.api.entity.Etiqueta;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.EtiquetaRepository;
import com.phantom.api.service.EtiquetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtiquetaServiceImpl implements EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaDTO> obtenerTodas() {
        return etiquetaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EtiquetaDTO obtenerPorId(Long id) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con el ID: " + id));
        return mapToDTO(etiqueta);
    }

    @Override
    @Transactional
    public EtiquetaDTO crear(EtiquetaRequestDTO request) {
        if (etiquetaRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una etiqueta con el nombre: " + request.getNombre());
        }
        Etiqueta etiqueta = Etiqueta.builder()
                .nombre(request.getNombre().trim())
                .build();
        return mapToDTO(etiquetaRepository.save(etiqueta));
    }

    @Override
    @Transactional
    public EtiquetaDTO actualizar(Long id, EtiquetaRequestDTO request) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con el ID: " + id));
        if (!etiqueta.getNombre().equalsIgnoreCase(request.getNombre().trim()) &&
                etiquetaRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Otro registro de etiqueta ya usa el nombre: " + request.getNombre());
        }
        etiqueta.setNombre(request.getNombre().trim());
        return mapToDTO(etiquetaRepository.save(etiqueta));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!etiquetaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Etiqueta no encontrada con el ID: " + id);
        }
        etiquetaRepository.deleteById(id);
    }

    private EtiquetaDTO mapToDTO(Etiqueta etiqueta) {
        return EtiquetaDTO.builder()
                .id(etiqueta.getId())
                .nombre(etiqueta.getNombre())
                .build();
    }
}
