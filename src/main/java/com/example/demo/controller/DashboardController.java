package com.example.demo.controller;

import com.example.demo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/tarefas")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        try {
            Map<String, Object> data = dashboardService.loadDashboardData(email);

            model.addAttribute("nomeUsuario", ((com.example.demo.model.Usuario) data.get("usuario")).getNome());
            model.addAttribute("total", data.get("total"));
            model.addAttribute("qtdPendentes", data.get("qtdPendentes"));
            model.addAttribute("qtdFazendo", data.get("qtdFazendo"));
            model.addAttribute("qtdConcluidas", data.get("qtdConcluidas"));
            model.addAttribute("qtdAlta", data.get("qtdAlta"));
            model.addAttribute("qtdMedia", data.get("qtdMedia"));
            model.addAttribute("qtdBaixa", data.get("qtdBaixa"));
            model.addAttribute("listaDeTarefas", data.get("listaDeTarefas"));
            model.addAttribute("listaTags", data.get("listaTags"));
            model.addAttribute("currentPage", "dashboard");

            return "index";
        } catch (Exception e) {
            log.error("Erro ao carregar dashboard: {}", e.getMessage());
            return "index";
        }
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        return "redirect:/tarefas";
    }
}