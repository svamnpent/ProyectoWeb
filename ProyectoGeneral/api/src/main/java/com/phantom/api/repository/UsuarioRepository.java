package com.phantom.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Crucial para la autenticación / Login
    Optional<Usuario> findByEmail(String email);

    // Validaciones rápidas de registro
    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    // Búsqueda general de usuarios para el panel de administración
    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.dni LIKE CONCAT('%', :keyword, '%')")
    Page<Usuario> buscarUsuariosAdmin(@Param("keyword") String keyword, Pageable pageable);
}