package com.utp.tienda.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.utp.tienda.model.Rol;
import com.utp.tienda.model.Usuario;
import com.utp.tienda.repository.RolRepository;
import com.utp.tienda.repository.UsuarioRepository;

@Configuration
public class DatosSeguridadIniciales {

	@Bean
	CommandLineRunner cargarUsuarios(
			RolRepository rolRepository,
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			Rol rolUser = rolRepository.findByNombre("ROLE_USER")
					.orElseGet(() -> rolRepository.save(new Rol("ROLE_USER")));

			Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
					.orElseGet(() -> rolRepository.save(new Rol("ROLE_ADMIN")));

			if (usuarioRepository.findByUsername("usuario").isEmpty()) {
				Usuario usuario = new Usuario(
						"usuario",
						passwordEncoder.encode("Usuario123*"),
						true);
				usuario.setRoles(Set.of(rolUser));
				usuarioRepository.save(usuario);
			}

			if (usuarioRepository.findByUsername("admin").isEmpty()) {
				Usuario admin = new Usuario(
						"admin",
						passwordEncoder.encode("Admin123*"),
						true);
				admin.setRoles(Set.of(rolAdmin, rolUser));
				usuarioRepository.save(admin);
			}
		};
	}
}
