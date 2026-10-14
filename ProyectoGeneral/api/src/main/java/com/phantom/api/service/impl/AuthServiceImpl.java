package com.phantom.api.service.impl;

import com.phantom.api.dto.AuthResponseDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.LoginRequestDTO;
import com.phantom.api.dto.request.RegisterRequestDTO;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.TooManyRequestsException;
import com.phantom.api.mapper.UsuarioMapper;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.security.CurrentUser;
import com.phantom.api.security.JwtService;
import com.phantom.api.security.LoginAttemptService;
import com.phantom.api.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String MENSAJE_CREDENCIALES = "Credenciales inválidas";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final CurrentUser currentUser;

    /**
     * Hash de relleno: se compara contra él cuando el email no existe para que el tiempo de
     * respuesta no revele qué emails están registrados.
     */
    private final String hashRelleno;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           LoginAttemptService loginAttemptService,
                           CurrentUser currentUser) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.currentUser = currentUser;
        this.hashRelleno = passwordEncoder.encode("relleno-para-igualar-tiempos");
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + email);
        }
        if (request.getDni() != null && usuarioRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + request.getDni());
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol("USER") // el registro público NUNCA puede crear administradores
                .dni(request.getDni())
                .direccion(request.getDireccion())
                .numeroTelefono(request.getNumeroTelefono())
                .build();
        return construirRespuesta(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (loginAttemptService.estaBloqueado(email)) {
            throw new TooManyRequestsException(
                    "Demasiados intentos fallidos. Inténtalo de nuevo más tarde");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        boolean tienePassword = usuario != null && usuario.getPassword() != null;
        String hash = tienePassword ? usuario.getPassword() : hashRelleno;
        boolean coincide = passwordEncoder.matches(request.getPassword(), hash);

        if (!tienePassword || !coincide) {
            loginAttemptService.registrarFallo(email);
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }

        loginAttemptService.registrarExito(email);
        return construirRespuesta(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO me() {
        Long id = currentUser.getId();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el ID: " + id));
        return UsuarioMapper.toDTO(usuario);
    }

    private AuthResponseDTO construirRespuesta(Usuario usuario) {
        return AuthResponseDTO.builder()
                .accessToken(jwtService.generarToken(usuario))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiracionSegundos())
                .build();
    }
}
