package com.phantom.api.repository;

import com.tienda.model.Perfil;
import com.tienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    // Obtener perfil directamenete desde el objeto usuario o su ID
    Optional<Perfil> findByUsuario(Usuario usuario);

    Optional<Perfil> findByUsuarioId(Long usuarioId);
}