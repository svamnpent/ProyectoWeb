package com.phantom.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.phantom.api.entity.Etiqueta;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    List<Etiqueta> findByIdIn(List<Long> ids);

    Optional<Etiqueta> findByNombreIgnoreCase(String nombre);

    List<Etiqueta> findByNombreIn(List<String> nombres);

    boolean existsByNombreIgnoreCase(String nombre);
}
