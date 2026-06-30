package com.example.demo.service;

import com.example.demo.config.ApplicationProperties;
import com.example.demo.dto.JwtTokenResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtilService {

    private final ApplicationProperties properties;
    private long refreshTokenDuration = 604800000L; // 7 dias

    /**
     * Gera Access Token JWT
     */
    public String generateToken(String email, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        return createToken(claims, email);
    }

    /**
     * Método compatível com código antigo
     */
    public JwtTokenResponseDTO gerarTokens(String email, String role, Long userId) {
        String accessToken = generateToken(email, role, userId);
        String refreshToken = UUID.randomUUID().toString();

        return JwtTokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        String secret = properties.getJwt().getSecret();
        Long expiration = properties.getJwt().getExpirationMs();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        String secret = properties.getJwt().getSecret();
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrai claims do token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ NOVO: Retorna os Claims do token (usado pelo JwtAuthenticationFilter)
     */
    public Claims getClaims(String token) {
        return extractAllClaims(token);
    }

    /**
     * Extrai email do token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai data de expiração
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica se o token está expirado
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida o token (método antigo)
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * ✅ NOVO: Valida se o token é válido para o usuário (usado pelo JwtAuthenticationFilter)
     */
    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("❌ Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gera Refresh Token (JWT)
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenDuration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("❌ Refresh token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrai email do refresh token
     */
    public String getEmailFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}