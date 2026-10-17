package com.phantom.api.service.impl;

import com.phantom.api.dto.UsuarioDTO;
import com.phantom.api.dto.request.UsuarioRequestDTO;
import com.phantom.api.entity.Perfil;
import com.phantom.api.entity.Usuario;
import com.phantom.api.exception.ResourceNotFoundException;
import com.phantom.api.repository.PerfilRepository;
import com.phantom.api.repository.UsuarioRepository;
import com.phantom.api.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private UsuarioServiceImpl service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).nombre("Ana").email("ana@x.com")
                .password("HASH_VIEJO").rol("USER").dni("12345678").build();
    }

    private UsuarioRequestDTO.UsuarioRequestDTOBuilder request() {
        return UsuarioRequestDTO.builder()
                .nombre(" Ana ").email(" ANA@X.COM ").password("Secreta123").dni("12345678");
    }

    @Test
    @DisplayName("crear - hashea el password, normaliza el email y asigna USER por defecto")
    void crear_ok() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(passwordEncoder.encode("Secreta123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO dto = service.crear(request().build());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertEquals("HASH", guardado.getPassword());
        assertNotEquals("Secreta123", guardado.getPassword());
        assertEquals("ana@x.com", guardado.getEmail());
        assertEquals("Ana", guardado.getNombre());
        assertEquals("USER", guardado.getRol());
        assertEquals("USER", dto.getRol());
    }

    @Test
    @DisplayName("crear - el rol se normaliza a mayúsculas")
    void crear_rolEnMinusculas() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO dto = service.crear(request().rol("admin").build());

        assertEquals("ADMIN", dto.getRol());
    }

    @Test
    @DisplayName("crear - se puede dar de alta personal con rol TRABAJADOR")
    void crear_trabajador() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO dto = service.crear(request().rol("trabajador").build());

        assertEquals("TRABAJADOR", dto.getRol());
    }

    @Test
    @DisplayName("actualizar - un TRABAJADOR no puede ascenderse a ADMIN")
    void actualizar_trabajadorNoSeAscienda() {
        usuario.setRol("TRABAJADOR");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(currentUser.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.actualizar(1L, request().rol("ADMIN").build()));
        assertEquals("TRABAJADOR", usuario.getRol());
    }

    @Test
    @DisplayName("crear - rol inválido lanza IllegalArgumentException")
    void crear_rolInvalido() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.crear(request().rol("ROOT").build()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - email duplicado (sin distinguir mayúsculas)")
    void crear_emailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.crear(request().build()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - DNI duplicado")
    void crear_dniDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@x.com")).thenReturn(false);
        when(usuarioRepository.existsByDni("12345678")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.crear(request().build()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - un usuario NO admin no puede cambiarse el rol (escalada de privilegios)")
    void actualizar_noAdminNoPuedeCambiarRol() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(currentUser.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.actualizar(1L, request().rol("ADMIN").build()));

        assertEquals("USER", usuario.getRol());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - un ADMIN sí puede cambiar el rol")
    void actualizar_adminCambiaRol() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(currentUser.isAdmin()).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO dto = service.actualizar(1L, request().rol("ADMIN").build());

        assertEquals("ADMIN", dto.getRol());
    }

    @Test
    @DisplayName("actualizar - mismo rol de un usuario normal es válido y no consulta permisos")
    void actualizar_mismoRolNoRequiereAdmin() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO dto = service.actualizar(1L, request().rol("user").build());

        assertEquals("USER", dto.getRol());
        verify(currentUser, never()).isAdmin();
    }

    @Test
    @DisplayName("actualizar - hashea el nuevo password; si viene null conserva el actual")
    void actualizar_password() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("HASH_NUEVO");

        service.actualizar(1L, request().password("NuevaClave123").build());
        assertEquals("HASH_NUEVO", usuario.getPassword());

        service.actualizar(1L, request().password(null).build());
        assertEquals("HASH_NUEVO", usuario.getPassword());
        verify(passwordEncoder, times(1)).encode(any());
    }

    @Test
    @DisplayName("actualizar - email o DNI ya usados por otro usuario")
    void actualizar_duplicados() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCase("otro@x.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, request().email("otro@x.com").build()));

        when(usuarioRepository.existsByDni("99999999")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, request().dni("99999999").build()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - inexistente")
    void actualizar_inexistente() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.actualizar(9L, request().build()));
    }

    @Test
    @DisplayName("eliminar - borra el perfil antes que el usuario")
    void eliminar_conPerfil() {
        Perfil perfil = Perfil.builder().id(3L).usuario(usuario).build();
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(perfilRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));

        service.eliminar(1L);

        InOrder orden = inOrder(perfilRepository, usuarioRepository);
        orden.verify(perfilRepository).delete(perfil);
        orden.verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar - inexistente")
    void eliminar_inexistente() {
        when(usuarioRepository.existsById(9L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(9L));
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("lecturas - no exponen el password y buscarPorEmail ignora mayúsculas")
    void lecturas() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmailIgnoreCase("ANA@x.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertEquals(1, service.obtenerTodas().size());
        assertEquals("ana@x.com", service.obtenerPorId(1L).getEmail());
        assertEquals(1L, service.buscarPorEmail(" ANA@x.com ").getId());
        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(9L));
    }
}
