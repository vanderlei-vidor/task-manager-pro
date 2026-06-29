package com.example.demo.controller;

import com.example.demo.model.Tag;
import com.example.demo.model.Task;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final UsuarioRepository usuarioRepository;
    private final TaskRepository taskRepository;

    @GetMapping("/tags")
    public String tagsPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<Tag> tags = tagService.listarTagsDoUsuario(email);
            List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());

            Map<Long, Long> tagUsageCount = new HashMap<>();
            tags.forEach(tag -> {
                long count = todasTasks.stream()
                        .filter(task -> task.getTags().contains(tag))
                        .count();
                tagUsageCount.put(tag.getId(), count);
            });

            model.addAttribute("nomeUsuario", usuario.getNome());
            model.addAttribute("tags", tags);
            model.addAttribute("tagUsageCount", tagUsageCount);
            model.addAttribute("totalTags", tags.size());
            model.addAttribute("totalTasks", todasTasks.size());
            model.addAttribute("currentPage", "tags");

            return "tags";
        } catch (Exception e) {
            log.error("Erro ao carregar página de tags: {}", e.getMessage());
            return "tags";
        }
    }

    @PostMapping("/tags/salvar")
    public String salvarTag(
            @RequestParam String name,
            @RequestParam(defaultValue = "#6c757d") String color,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            tagService.criarTag(name, color, authentication.getName());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Tag '" + name + "' criada com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao criar tag: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", " Erro ao criar tag: " + e.getMessage());
        }
        return "redirect:/tags";
    }

    @GetMapping("/tags/excluir/{id}")
    public String excluirTag(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            tagService.deletarTag(id, authentication.getName());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Tag excluída!");
        } catch (Exception e) {
            log.error("Erro ao excluir tag: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao excluir tag: " + e.getMessage());
        }
        return "redirect:/tags";
    }
}