package com.example.demo.service;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.BusinessRuleViolationException;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service para gerenciar o perfil do usuário
 * Responsabilidade única: operações relacionadas ao perfil
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Busca perfil completo do usuário
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String email) {
        log.info("👤 Buscando perfil do usuário: {}", email);
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return UserProfileDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .dataCadastro(usuario.getDataCriacao())
                .totalTasks(usuario.getTarefas() != null ? usuario.getTarefas().size() : 0)
                .build();
    }

    /**
     * Atualiza dados do perfil (nome e email)
     */
    @Transactional
    public UserProfileDTO updateProfile(String email, UserProfileDTO dto) {
        log.info("✏️ Atualizando perfil do usuário: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // Validação: email único
        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BusinessRuleViolationException(
                        BusinessRuleViolationException.ViolationType.DUPLICATE_EMAIL,
                        "Este e-mail já está em uso!"
                );
            }
            usuario.setEmail(dto.getEmail());
        }

        // Atualizar nome
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuario.setNome(dto.getNome());
        }

        Usuario atualizado = usuarioRepository.save(usuario);
        log.info("✅ Perfil atualizado com sucesso: {}", email);

        return UserProfileDTO.builder()
                .id(atualizado.getId())
                .nome(atualizado.getNome())
                .email(atualizado.getEmail())
                .dataCadastro(atualizado.getDataCriacao())
                .totalTasks(atualizado.getTarefas() != null ? atualizado.getTarefas().size() : 0)
                .build();
    }

    /**
     * Troca a senha do usuário
     */
    @Transactional
    public void changePassword(String email, ChangePasswordDTO dto) {
        log.info("🔐 Alterando senha do usuário: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // Validar senha atual
        if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new BusinessRuleViolationException(
                    BusinessRuleViolationException.ViolationType.INVALID_PASSWORD,
                    "Senha atual incorreta!"
            );
        }

        // Validar confirmação
        if (!dto.getNovaSenha().equals(dto.getConfirmarSenha())) {
            throw new BusinessRuleViolationException(
                    BusinessRuleViolationException.ViolationType.PASSWORD_MISMATCH,
                    "As senhas não coincidem!"
            );
        }

        // Validar força da senha
        if (dto.getNovaSenha().length() < 6) {
            throw new BusinessRuleViolationException(
                    BusinessRuleViolationException.ViolationType.WEAK_PASSWORD,
                    "A nova senha deve ter no mínimo 6 caracteres!"
            );
        }

        // Atualizar senha
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);

        log.info("✅ Senha alterada com sucesso: {}", email);
    }
}