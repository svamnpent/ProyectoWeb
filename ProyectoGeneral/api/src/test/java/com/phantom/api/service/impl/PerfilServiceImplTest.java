package com.phantom.api.service.impl;

import com.phantom.api.dto.PerfilDTO;
import com.phantom.api.dto.request.PerfilRequestDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceImplTest {

    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private PerfilServiceImpl service;

    private Usuario dueno;
    private Perfil perfil;

    @BeforeEach
    void setUp() {
        dueno = Usuario.builder().id(1L).nombre("Ana").email("ana@x.com").build();
        perfil = Perfil.builder().id(5L).avatarUrl("a.png").biografia("hola").usuario(dueno).build();
    }

    private PerfilRequestDTO request(Long usuarioId) {
        return PerfilRequestDTO.builder().avatarUrl("b.png").biografia("nueva").usuarioId(usuarioId).build();
    }

    @Test
    @DisplayName("obtenerPorId - valida que sea el dueño o ADMIN")
    void obtenerPorId_ok() {
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));

        PerfilDTO dto = service.obtenerPorId(5L);

        assertEquals(1L, dto.getUsuarioId());
        verify(currentUser).requireSelfOrAdmin(1L);
    }

    @Test
    @DisplayName("obtenerPorId - perfil ajeno es rechazado")
    void obtenerPorId_ajeno() {
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));
        doThrow(new AccessDeniedException("no")).when(currentUser).requireSelfOrAdmin(1L);

        assertThrows(AccessDeniedException.class, () -> service.obtenerPorId(5L));
    }

    @Test
    @DisplayName("obtenerPorId - inexistente")
    void obtenerPorId_inexistente() {
        when(perfilRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(9L));
    }

    @Test
    @DisplayName("crear - no se puede crear el perfil de otro usuario")
    void crear_deOtroUsuario() {
        doThrow(new AccessDeniedException("no")).when(currentUser).requireSelfOrAdmin(2L);

        assertThrows(AccessDeniedException.class, () -> service.crear(request(2L)));

        verifyNoInteractions(usuarioRepository);
        verify(perfilRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear - perfil propio")
    void crear_ok() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(dueno));
        when(perfilRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(perfilRepository.save(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        PerfilDTO dto = service.crear(request(1L));

        assertEquals("b.png", dto.getAvatarUrl());
        assertEquals(1L, dto.getUsuarioId());
    }

    @Test
    @DisplayName("crear - el usuario ya tiene perfil")
    void crear_yaTienePerfil() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(dueno));
        when(perfilRepository.findByUsuarioId(1L)).thenReturn(Optional.of(perfil));

        assertThrows(IllegalArgumentException.class, () -> service.crear(request(1L)));
        verify(perfilRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - un usuario normal no puede reasignar su perfil a otro usuario")
    void actualizar_reasignarNoAdmin() {
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));
        when(currentUser.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.actualizar(5L, request(2L)));
        verify(perfilRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar - un ADMIN puede reasignar el perfil")
    void actualizar_reasignarAdmin() {
        Usuario otro = Usuario.builder().id(2L).nombre("Luis").email("l@x.com").build();
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));
        when(currentUser.isAdmin()).thenReturn(true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otro));
        when(perfilRepository.findByUsuarioId(2L)).thenReturn(Optional.empty());
        when(perfilRepository.save(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        PerfilDTO dto = service.actualizar(5L, request(2L));

        assertEquals(2L, dto.getUsuarioId());
    }

    @Test
    @DisplayName("actualizar - datos propios sin reasignar")
    void actualizar_propio() {
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        PerfilDTO dto = service.actualizar(5L, request(1L));

        assertEquals("nueva", dto.getBiografia());
        verify(currentUser, never()).isAdmin();
    }

    @Test
    @DisplayName("eliminar - valida dueño y borra")
    void eliminar_ok() {
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));

        service.eliminar(5L);

        verify(currentUser).requireSelfOrAdmin(1L);
        verify(perfilRepository).delete(perfil);
    }

    @Test
    @DisplayName("eliminar - perfil ajeno es rechazado y no se borra")
    void eliminar_ajeno() {
        when(perfilRepository.findById(5L)).thenReturn(Optional.of(perfil));
        doThrow(new AccessDeniedException("no")).when(currentUser).requireSelfOrAdmin(1L);

        assertThrows(AccessDeniedException.class, () -> service.eliminar(5L));
        verify(perfilRepository, never()).delete(any());
    }

    @Test
    @DisplayName("eliminar - inexistente")
    void eliminar_inexistente() {
        when(perfilRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(9L));
    }
}
