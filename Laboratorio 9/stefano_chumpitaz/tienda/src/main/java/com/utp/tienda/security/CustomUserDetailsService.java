package com.utp.tienda.security;

import com.utp.tienda.model.Usuario;
import com.utp.tienda.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String[] autoridades = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .toArray(String[]::new);

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(autoridades)
                .disabled(!usuario.isActivo())
                .build();
    }
}