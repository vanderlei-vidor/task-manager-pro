package com.example.demo.controller;

import com.example.demo.dto.JwtTokenResponseDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.RegistroUsuarioDTO;
import com.example.demo.model.RefreshToken; // ✅ NOVO
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.AutenticacaoService;
import com.example.demo.service.JwtUtilService;
import com.example.demo.service.RefreshTokenService; // ✅ NOVO
import com.example.demo.exception.AuthException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j; // ✅ ADICIONAR
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // ✅ ADICIONAR
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j // ✅ ADICIONAR AQUI!
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtilService jwtUtilService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Endpoint de Login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO dto) {
        Usuario usuario = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AuthException(AuthException.ReasonType.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new AuthException(AuthException.ReasonType.PASSWORD_DOES_NOT_MATCH);
        }

        // ✅ Gera Access Token (método existente)
        JwtTokenResponseDTO tokens = jwtUtilService.gerarTokens(
                usuario.getEmail(),
                "ROLE_USER",
                usuario.getId());

        // ✅ NOVO: Gera Refresh Token (UUID armazenado no banco)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario.getEmail());

        log.info("✅ Login realizado com sucesso: {}", usuario.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());
        response.put("accessToken", tokens.getAccessToken());
        response.put("refreshToken", refreshToken.getToken()); // ✅ Token UUID
        response.put("expiresIn", 900000);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de Registro (Cadastro)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegistroUsuarioDTO dto) {
        // ✅ Verifica se já existe usuário com o mesmo email
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AuthException(AuthException.ReasonType.USER_ALREADY_EXISTS); // ✅ CORRIGIDO!
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setNome(dto.getNome());

        repository.save(novoUsuario);

        Map<String, Object> response = new HashMap<>();
        response.put("nome", novoUsuario.getNome());
        response.put("email", novoUsuario.getEmail());
        response.put("message", "Usuário cadastrado com sucesso!");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para renovar access token usando refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        log.info("🔄 [DEBUG] Requisição de refresh recebida");
        log.info("🔄 [DEBUG] Body: {}", request);

        String refreshTokenStr = request.get("refreshToken");
        log.info("🔄 [DEBUG] Refresh token extraído: {}", refreshTokenStr);

        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            log.error("❌ [DEBUG] Refresh token está null ou vazio!");
            throw new AuthException(AuthException.ReasonType.INVALID_TOKEN);
        }

        try {
            log.info("🔄 [DEBUG] Buscando token no banco...");
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
            log.info("🔄 [DEBUG] Token encontrado! ID: {}, Revoked: {}, Expiry: {}",
                    refreshToken.getId(),
                    refreshToken.isRevoked(),
                    refreshToken.getExpiryDate());

            log.info("🔄 [DEBUG] Validando expiração...");
            refreshTokenService.verifyExpiration(refreshToken);
            log.info("🔄 [DEBUG] Token válido!");

            log.info("🔄 [DEBUG] Rotacionando token...");
            RefreshToken newRefreshToken = refreshTokenService.rotateToken(refreshToken);
            log.info("🔄 [DEBUG] Novo token criado! ID: {}", newRefreshToken.getId());

            Usuario usuario = refreshToken.getUsuario();
            log.info("🔄 [DEBUG] Gerando novo access token para: {}", usuario.getEmail());

            String newAccessToken = jwtUtilService.generateToken(
                    usuario.getEmail(),
                    "ROLE_USER",
                    usuario.getId());

            log.info("✅ [DEBUG] Token renovado com sucesso para: {}", usuario.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken.getToken());
            response.put("expiresIn", 900000);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ [DEBUG] ERRO ao renovar token: {}", e.getMessage(), e);
            throw new AuthException(AuthException.ReasonType.INVALID_TOKEN);
        }
    }

    /**
     * Endpoint de Logout - Revoga todos os tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        String email = authentication.getName();

        try {
            refreshTokenService.revokeAllUserTokens(email);
            log.info("🚪 Logout realizado: {}", email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout realizado com sucesso!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro no logout: {}", e.getMessage());
            throw new AuthException(AuthException.ReasonType.LOGOUT_FAILED);
        }
    }
}