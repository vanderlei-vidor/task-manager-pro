package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para as PÁGINAS de autenticação (Thymeleaf)
 * Diferente do AuthController que é uma API REST
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Página de Login
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Página de Cadastro
     */
    @GetMapping("/cadastrar")
    public String cadastrarPage() {
        return "cadastrar";
    }

    /**
     * Processar Cadastro (formulário HTML)
     */
    @PostMapping("/cadastrar")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            RedirectAttributes redirectAttributes) {
        try {
            if (usuarioRepository.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "E-mail já cadastrado!");
                return "redirect:/cadastrar";
            }

            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(nome);
            novoUsuario.setEmail(email);
            novoUsuario.setSenha(passwordEncoder.encode(senha));
            usuarioRepository.save(novoUsuario);

            log.info("✅ Novo usuário cadastrado: {}", email);
            redirectAttributes.addAttribute("sucesso", true);
            return "redirect:/login";

        } catch (Exception e) {
            log.error("❌ Erro ao cadastrar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao cadastrar: " + e.getMessage());
            return "redirect:/cadastrar";
        }
    }
}