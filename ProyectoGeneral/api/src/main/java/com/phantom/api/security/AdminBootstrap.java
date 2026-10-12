package com.phantom.api.security;

import com.phantom.api.entity.Usuario;
import com.phantom.api.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial al arrancar si ADMIN_EMAIL y ADMIN_PASSWORD están definidos
 * y todavía no existe un usuario con ese email. Nunca modifica un usuario existente.
 */
@Slf4j
@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String nombre;

    public AdminBootstrap(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap.admin.email:}") String email,
                          @Value("${app.bootstrap.admin.password:}") String password,
                          @Value("${app.bootstrap.admin.nombre:Administrador}") String nombre) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD no definidos: no se crea administrador inicial");
            return;
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            log.info("El administrador inicial ya existe ({}): no se modifica", emailNormalizado);
            return;
        }
        if (password.length() < 8) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos 8 caracteres");
        }
        usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .email(emailNormalizado)
                .password(passwordEncoder.encode(password))
                .rol("ADMIN")
                .build());
        log.info("Administrador inicial creado: {}", emailNormalizado);
    }
}
