package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TaskRepository; // 🚀 Injetado
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.ExcelService;
import com.example.demo.service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ExportController {

    private final UsuarioRepository usuarioRepository;
    private final TaskRepository taskRepository; // 🚀 Adicionado o repositório de tarefas
    private final PdfService pdfService;
    private final ExcelService excelService;

    @GetMapping("/tarefas/pdf")
    public void exportarParaPdf(
            HttpServletResponse response,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 🔥 CORREÇÃO: Busca direto do repositório trazendo as tags e evitando o Lazy Error
        List<Task> tarefas = taskRepository.findByUsuarioIdWithTags(usuario.getId());

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=tarefas.pdf");
        pdfService.gerarPdfTarefas(response, tarefas);
    }

    @GetMapping("/tarefas/excel")
    public void exportarParaExcel(
            HttpServletResponse response,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 🔥 CORREÇÃO: Mesmo ajuste aqui para blindar o download do Excel
        List<Task> tarefas = taskRepository.findByUsuarioIdWithTags(usuario.getId());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tarefas.xlsx");
        excelService.gerarExcelTarefas(response, tarefas);
    }
}