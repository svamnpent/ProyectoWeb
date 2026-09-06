package com.phantom.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.phantom.api.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}