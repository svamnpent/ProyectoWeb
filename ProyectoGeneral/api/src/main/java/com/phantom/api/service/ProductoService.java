package com.phantom.api.service;

import com.phantom.api.dto.ProductoDTO;
import com.phantom.api.dto.request.ProductoRequestDTO;

import java.util.List;

public interface ProductoService {
    List<ProductoDTO> obtenerTodas();

    ProductoDTO obtenerPorId(Long id);

    List<ProductoDTO> buscarPorPalabraClave(String keyword);

    ProductoDTO crear(ProductoRequestDTO request);

    ProductoDTO actualizar(Long id, ProductoRequestDTO request);

    void eliminar(Long id);
}
