package com.utp.tienda.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.utp.tienda.model.Rol;
import com.utp.tienda.model.Usuario;
import com.utp.tienda.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private CustomUserDetailsService service;

	@Test
	void debeConvertirRolesEnAutoridades() {
		Usuario admin = new Usuario("admin", "$2a$hash", true);
		admin.setRoles(Set.of(new Rol("ROLE_ADMIN"), new Rol("ROLE_USER")));
		when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

		UserDetails detalles = service.loadUserByUsername("admin");

		assertEquals("admin", detalles.getUsername());
		assertTrue(detalles.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
		assertTrue(detalles.isEnabled());
	}

	@Test
	void usuarioInactivoDebeQuedarDeshabilitado() {
		Usuario usuario = new Usuario("usuario", "$2a$hash", false);
		usuario.setRoles(Set.of(new Rol("ROLE_USER")));
		when(usuarioRepository.findByUsername("usuario")).thenReturn(Optional.of(usuario));

		UserDetails detalles = service.loadUserByUsername("usuario");

		assertFalse(detalles.isEnabled());
	}

	@Test
	void usuarioInexistenteDebeLanzarExcepcion() {
		when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nadie"));
	}
}
