package com.example.demo.controller;

import com.example.demo.dto.TaskDTO;
import com.example.demo.model.TaskPriority;
import com.example.demo.model.TaskStatus;
import com.example.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TaskPageController {

    private final TaskService taskService;

    @PostMapping("/tarefas/salvar")
    public String salvarTarefa(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "TODO") String status,
            @RequestParam String dueDate,
            @RequestParam(defaultValue = "MEDIUM") String priority,
            @RequestParam(required = false) List<Long> tagIds,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            TaskDTO dto = TaskDTO.builder()
                    .title(title)
                    .description(description)
                    .status(TaskStatus.valueOf(status))
                    .priority(TaskPriority.valueOf(priority))
                    .dueDate(LocalDate.parse(dueDate))
                    .tagIds(tagIds != null ? new HashSet<>(tagIds) : null)
                    .build();

            taskService.criarTask(dto, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Tarefa '" + title + "' criada com sucesso!");

        } catch (Exception e) {
            log.error("Erro ao salvar tarefa: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao criar tarefa: " + e.getMessage());
        }
        return "redirect:/tarefas";
    }

    @PostMapping("/tarefas/concluir/{id}")
    public String concluirTarefa(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            taskService.atualizarStatus(id, TaskStatus.DONE, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Tarefa concluída! 🎉");
        } catch (Exception e) {
            log.error("Erro ao concluir tarefa: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao concluir: " + e.getMessage());
        }
        return "redirect:/tarefas";
    }

    @PostMapping("/tarefas/editar/{id}")
    public String editarTarefa(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String status,
            @RequestParam String dueDate,
            @RequestParam(defaultValue = "MEDIUM") String priority,
            @RequestParam(required = false) List<Long> tagIds,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            TaskDTO dto = TaskDTO.builder()
                    .title(title)
                    .description(description)
                    .status(TaskStatus.valueOf(status))
                    .priority(TaskPriority.valueOf(priority))
                    .dueDate(LocalDate.parse(dueDate))
                    .tagIds(tagIds != null ? new HashSet<>(tagIds) : null)
                    .build();

            taskService.atualizarTask(id, dto, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Tarefa atualizada com sucesso!");

        } catch (Exception e) {
            log.error("Erro ao editar tarefa: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao editar: " + e.getMessage());
        }
        return "redirect:/tarefas";
    }

    @GetMapping("/tarefas/excluir/{id}")
    public String excluirTarefa(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            taskService.deletarTask(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Tarefa excluída!");
        } catch (Exception e) {
            log.error("Erro ao excluir tarefa: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/tarefas";
    }

    @PostMapping("/tarefas/mover/{id}")
    public String moverTask(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            taskService.atualizarStatus(id, TaskStatus.valueOf(status), userDetails.getUsername());
            redirectAttributes.addFlashAttribute("mensagemSucesso", "✅ Task movida com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao mover task: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensagemErro", "❌ Erro: " + e.getMessage());
        }
        return "redirect:/kanban";
    }
}