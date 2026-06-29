package com.example.demo.service;

import com.example.demo.config.ApplicationProperties;
import com.example.demo.dto.JwtTokenResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
public class JwtUtilService {

    private final ApplicationProperties properties;

    // ✅ Injeta ApplicationProperties (ao invés de @Value separado)
    public JwtUtilService(ApplicationProperties properties) {
        this.properties = properties;
    }

    // ✅ Gera a chave de assinatura
    private SecretKey getSigningKey() {
        byte[] keyBytes = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        
        // ✅ Garante que a chave tenha pelo menos 32 bytes (256 bits) para HS256
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret deve ter pelo menos 32 caracteres! Tamanho atual: " + keyBytes.length
            );
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera Access Token + Refresh Token
     */
    public JwtTokenResponseDTO gerarTokens(String email, String role, Long userId) {
        Date now = new Date();
        
        // ✅ Access Token (curta duração - 24h)
        Date accessTokenExpiresAt = new Date(now.getTime() + properties.getJwtExpirationMs());
        String accessToken = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)  // ✅ Agora tem o ID real!
                .issuedAt(now)
                .expiration(accessTokenExpiresAt)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        // ✅ Refresh Token (longa duração - 7 dias)
        long refreshExpirationMs = 7L * 24 * 60 * 60 * 1000; // 7 dias em ms
        Date refreshTokenExpiresAt = new Date(now.getTime() + refreshExpirationMs);
        String refreshToken = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(refreshTokenExpiresAt)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        return new JwtTokenResponseDTO(accessToken, refreshToken);
    }

    /**
     * Extrai o email (subject) do token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a role do token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrai o userId do token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extrai data de expiração
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * ✅ MÉTODO QUE O FILTER USA! (CRÍTICO!)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            boolean isValid = email.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            if (isValid) {
                log.debug("Token válido para usuário: {}", email);
            }
            
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida se o token está válido (sem comparar com usuário)
     */
    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retorna os Claims do token
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica se o token expirou
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Método genérico para extrair qualquer claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }
}