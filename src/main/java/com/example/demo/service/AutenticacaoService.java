package com.example.demo.service;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UsuarioRepository repository;

    @Override
    @Transactional(readOnly = true)  // ✅ Otimização: só leitura
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Tentando autenticar usuário: {}", email);

        Usuario usuario = repository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado: {}", email);
                return new UsernameNotFoundException("Usuário não encontrado: " + email);
            });

        log.info("Usuário {} autenticado com sucesso", email);

        // ✅ Usa List.of() (Java 9+) - mais moderno que Collections.singletonList()
        // ✅ Preparado para múltiplas roles no futuro
        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getSenha())
            .authorities(List.of(
                new SimpleGrantedAuthority("ROLE_USER")
                // Futuro: new SimpleGrantedAuthority("ROLE_" + usuario.getRole().toUpperCase())
            ))
            .build();
    }
}