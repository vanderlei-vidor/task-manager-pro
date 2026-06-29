package com.example.demo.controller;

import com.example.demo.dto.ReportDTO;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/relatorios")
    public String relatorios(
            Authentication authentication, Model model,
            @RequestParam(defaultValue = "30") int days) {
        String email = authentication.getName();

        try {
            ReportDTO report = reportService.generateReport(email, days);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            model.addAttribute("nomeUsuario", usuario.getNome());
            model.addAttribute("report", report);
            model.addAttribute("selectedDays", days);
            model.addAttribute("currentPage", "relatorios");

            return "relatorios";
        } catch (Exception e) {
            log.error("Erro ao carregar relatórios: {}", e.getMessage());
            return "relatorios";
        }
    }
}