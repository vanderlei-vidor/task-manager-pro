package com.example.demo.controller;



import com.example.demo.dto.JwtTokenResponseDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.RegistroUsuarioDTO;
import com.example.demo.model.Usuario;
import com.example.demo.service.AutenticacaoService;
import com.example.demo.service.JwtUtilService;
import com.example.demo.exception.AuthException;
import com.example.demo.repository.UsuarioRepository;
import jakarta.validation.Valid;  // ✅ Adicionado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // ⚠️ Remover em produção!
public class AuthController {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtilService jwtUtilService;

    /**
     * Endpoint de Login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO dto) {
        // ✅ Usa ReasonType correto
        Usuario usuario = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AuthException(AuthException.ReasonType.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new AuthException(AuthException.ReasonType.PASSWORD_DOES_NOT_MATCH);
        }

        // ✅ Gera os tokens JWT
        JwtTokenResponseDTO tokens = jwtUtilService.gerarTokens(
                usuario.getEmail(),
                "ROLE_USER", // Ou pegar do banco se tiver campo role
                usuario.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("nome", usuario.getNome());
        response.put("email", usuario.getEmail());
        response.put("accessToken", tokens.getAccessToken());
        response.put("refreshToken", tokens.getRefreshToken());

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
}