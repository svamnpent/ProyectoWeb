package com.phantom.api.repository;

import com.tienda.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    Optional<Etiqueta> findByNombreIgnoreCase(String nombre);

    List<Etiqueta> findByNombreIn(List<String> nombres);

    boolean existsByNombreIgnoreCase(String nombre);
}