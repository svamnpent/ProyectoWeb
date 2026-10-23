package com.phantom.api.security;

import com.phantom.api.entity.Usuario;
import com.phantom.api.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminBootstrap bootstrap(String email, String password) {
        return new AdminBootstrap(usuarioRepository, passwordEncoder, email, password, "Admin");
    }

    @Test
    @DisplayName("sin ADMIN_EMAIL/ADMIN_PASSWORD no hace nada")
    void sinConfiguracion() {
        bootstrap("", "").run(null);
        bootstrap(null, null).run(null);

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("crea el ADMIN con password hasheado y email normalizado")
    void creaAdmin() {
        when(usuarioRepository.existsByEmailIgnoreCase("admin@x.com")).thenReturn(false);
        when(passwordEncoder.encode("Admin12345")).thenReturn("HASH");

        bootstrap(" ADMIN@x.com ", "Admin12345").run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario admin = captor.getValue();
        assertEquals("ADMIN", admin.getRol());
        assertEquals("admin@x.com", admin.getEmail());
        assertEquals("HASH", admin.getPassword());
    }

    @Test
    @DisplayName("si el usuario ya existe no lo modifica")
    void noModificaExistente() {
        when(usuarioRepository.existsByEmailIgnoreCase("admin@x.com")).thenReturn(true);

        bootstrap("admin@x.com", "Admin12345").run(null);

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("una contraseña de menos de 8 caracteres impide arrancar")
    void passwordCorta() {
        when(usuarioRepository.existsByEmailIgnoreCase("admin@x.com")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> bootstrap("admin@x.com", "corta").run(null));
        verify(usuarioRepository, never()).save(any());
    }
}
