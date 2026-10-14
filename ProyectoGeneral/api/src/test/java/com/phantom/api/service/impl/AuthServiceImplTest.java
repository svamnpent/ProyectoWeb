package com.phantom.api.service.impl;

import com.phantom.api.dto.AuthResponseDTO;
import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.LoginRequestDTO;
import com.phantom.api.dto.request.RegisterRequestDTO;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.exception.TooManyRequestsException;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.security.CurrentUser;
import com.phantom.api.security.JwtService;
import com.phantom.api.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private CurrentUser currentUser;

    // Encoder real (coste mínimo) para verificar el hash de verdad
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private AuthServiceImpl service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(usuarioRepository, passwordEncoder, jwtService, loginAttemptService,
                currentUser);
        usuario = Usuario.builder().id(1L).nombre("Ana").email("ana@x.com")
                .password(passwordEncoder.encode("Secreta123")).rol("USER").build();
    }

    private LoginRequestDTO login(String email, String password) {
        return LoginRequestDTO.builder().email(email).password(password).build();
    }

    private RegisterRequestDTO.RegisterRequestDTOBuilder registro() {
        return RegisterRequestDTO.builder().nombre(" Ana ").email(" ANA@X.COM ").password("Secreta123");
    }

    // ------------------------------------------------------------ register

    @Test
    @DisplayName("register - crea usuario USER con password hasheado y devuelve token")
    void register_ok() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("TOKEN");
        when(jwtService.getExpiracionSegundos()).thenReturn(3600L);

        AuthResponseDTO respuesta = service.register(registro().build());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("ana@x.com", guardado.getEmail());
        assertEquals("USER", guardado.getRol());
        assertNotEquals("Secreta123", guardado.getPassword());
        assertTrue(passwordEncoder.matches("Secreta123", guardado.getPassword()));
        assertEquals("TOKEN", respuesta.getAccessToken());
        assertEquals("Bearer", respuesta.getTokenType());
        assertEquals(3600L, respuesta.getExpiresIn());
    }

    @Test
    @DisplayName("register - email duplicado")
    void register_emailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.register(registro().build()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - DNI duplicado")
    void register_dniDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.register(registro().dni("12345678").build()));
        verify(usuarioRepository, never()).save(any());
    }

    // ------------------------------------------------------------ login

    @Test
    @DisplayName("login - credenciales correctas devuelven token y reinician el contador de fallos")
    void login_ok() {
        when(usuarioRepository.findByEmailIgnoreCase("ana@x.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(usuario)).thenReturn("TOKEN");
        when(jwtService.getExpiracionSegundos()).thenReturn(3600L);

        AuthResponseDTO respuesta = service.login(login(" ANA@x.com ", "Secreta123"));

        assertEquals("TOKEN", respuesta.getAccessToken());
        assertEquals(3600L, respuesta.getExpiresIn());
        verify(loginAttemptService).registrarExito("ana@x.com");
        verify(loginAttemptService, never()).registrarFallo(any());
    }

    @Test
    @DisplayName("login - password incorrecto: 401 genérico y se registra el fallo")
    void login_passwordIncorrecto() {
        when(usuarioRepository.findByEmailIgnoreCase("ana@x.com")).thenReturn(Optional.of(usuario));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> service.login(login("ana@x.com", "incorrecta")));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verify(loginAttemptService).registrarFallo("ana@x.com");
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("login - email inexistente: mismo mensaje que password incorrecto (no revela si existe)")
    void login_emailInexistente() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@x.com")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> service.login(login("nadie@x.com", "Secreta123")));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verify(loginAttemptService).registrarFallo("nadie@x.com");
    }

    @Test
    @DisplayName("login - usuario sin password (null) no puede autenticarse")
    void login_usuarioSinPassword() {
        usuario.setPassword(null);
        when(usuarioRepository.findByEmailIgnoreCase("ana@x.com")).thenReturn(Optional.of(usuario));

        assertThrows(BadCredentialsException.class, () -> service.login(login("ana@x.com", "cualquiera")));
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("login - cuenta bloqueada: 429 sin consultar la BD")
    void login_bloqueado() {
        when(loginAttemptService.estaBloqueado("ana@x.com")).thenReturn(true);

        assertThrows(TooManyRequestsException.class, () -> service.login(login("ana@x.com", "Secreta123")));

        verifyNoInteractions(usuarioRepository);
        verify(loginAttemptService, never()).registrarFallo(any());
    }

    // ------------------------------------------------------------ me

    @Test
    @DisplayName("me - devuelve el usuario autenticado")
    void me_ok() {
        when(currentUser.getId()).thenReturn(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioDTO dto = service.me();

        assertEquals("ana@x.com", dto.getEmail());
    }

    @Test
    @DisplayName("me - el usuario del token ya no existe")
    void me_usuarioEliminado() {
        when(currentUser.getId()).thenReturn(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.me());
    }
}
