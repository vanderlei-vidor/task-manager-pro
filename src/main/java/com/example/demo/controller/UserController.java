package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    /**
     * Página de perfil do usuário
     */
    @GetMapping
    public String perfilPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        
        try {
            UserProfileDTO profile = userProfileService.getProfile(email);
            
            model.addAttribute("profile", profile);
            model.addAttribute("profileDTO", profile);
            model.addAttribute("passwordDTO", new ChangePasswordDTO());
            model.addAttribute("currentPage", "perfil");
            
            return "perfil";
        } catch (Exception e) {
            log.error("Erro ao carregar perfil: {}", e.getMessage());
            return "redirect:/tarefas";
        }
    }

    /**
     * Atualizar dados do perfil
     */
    @PostMapping("/atualizar")
    public String atualizarPerfil(
            @Valid @ModelAttribute("profileDTO") UserProfileDTO dto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Verifique os dados informados!");
            return "redirect:/perfil";
        }

        try {
            userProfileService.updateProfile(authentication.getName(), dto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Perfil atualizado com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao atualizar perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ " + e.getMessage());
        }

        return "redirect:/perfil";
    }

    /**
     * Alterar senha
     */
    @PostMapping("/alterar-senha")
    public String alterarSenha(
            @Valid @ModelAttribute("passwordDTO") ChangePasswordDTO dto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Verifique os dados da senha!");
            return "redirect:/perfil";
        }

        try {
            userProfileService.changePassword(authentication.getName(), dto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "🔐 Senha alterada com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao alterar senha: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ " + e.getMessage());
        }

        return "redirect:/perfil";
    }
}