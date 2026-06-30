package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.jwt.refresh-token-duration:604800000}")
    private long refreshTokenDurationMs;

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        log.info("🔑 Criando refresh token para: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("✅ Refresh token criado com sucesso! ID: {}", refreshToken.getId());

        return refreshToken;
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            log.warn("⚠️ Refresh token expirado: {}", token.getToken());
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            // ✅ CORRIGIDO: Usando enum + mensagem customizada
            throw new InvalidTokenException(
                InvalidTokenException.ReasonType.EXPIRED_TOKEN,
                "Refresh token expirado! Faça login novamente."
            );
        }

        if (token.isRevoked()) {
            log.warn("⚠️ Refresh token revogado: {}", token.getToken());
            // ✅ CORRIGIDO: Usando enum + mensagem customizada
            throw new InvalidTokenException(
                InvalidTokenException.ReasonType.REVOKED_TOKEN,
                "Refresh token revogado! Faça login novamente."
            );
        }

        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh token não encontrado"));
    }

    /**
     * Rotaciona o refresh token (Revoga o antigo e gera um novo)
     * ✅ CORRIGIDO: Busca usuário novamente para evitar LazyInitializationException
     */
    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        // ✅ BUSCAR USUÁRIO NOVAMENTE (evita proxy lazy)
        Long userId = oldToken.getUsuario().getId();
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        
        log.info("🔄 Rotacionando refresh token para o usuário: {}", usuario.getEmail());
        
        // Revoga o token atual
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Retorna um novo token novinho em folha
        return createRefreshToken(usuario.getEmail());
    }

    @Transactional
    public void revokeAllUserTokens(String email) {
        log.info("🚫 Revogando todos os tokens do usuário: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        refreshTokenRepository.revokeAllUserTokens(usuario);
        log.info("✅ Todos os tokens revogados!");
    }

    @Transactional
    public void revokeToken(String token) {
        log.info("🚫 Revogando token específico...");
        refreshTokenRepository.revokeByToken(token);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("🧹 Limpando tokens expirados...");
        long deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("🗑️ Faxina concluída. Foram apagados {} tokens obsoletos do banco.", deletedCount);
    }

    @Transactional(readOnly = true)
    public long countActiveTokens(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return refreshTokenRepository.countByUsuarioAndRevokedFalse(usuario);
    }
}