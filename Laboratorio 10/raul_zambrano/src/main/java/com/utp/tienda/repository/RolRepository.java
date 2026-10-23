package com.utp.tienda.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.tienda.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
	Optional<Rol> findByNombre(String nombre);
}
