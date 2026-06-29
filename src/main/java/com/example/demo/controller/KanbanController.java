package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.model.TaskPriority;
import com.example.demo.model.TaskStatus;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;  
import org.springframework.web.bind.annotation.ResponseBody;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;

import com.example.demo.exception.NotFoundException;


@Slf4j
@Controller
@RequiredArgsConstructor
public class KanbanController {

    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagService tagService;

    @GetMapping("/kanban")
    public String kanban(Authentication authentication, Model model) {
        String email = authentication.getName();
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());

            List<Task> todoTasks = todasTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.TODO)
                    .sorted((a, b) -> b.getPriority().getOrder() - a.getPriority().getOrder())
                    .collect(Collectors.toList());

            List<Task> doingTasks = todasTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DOING)
                    .sorted((a, b) -> b.getPriority().getOrder() - a.getPriority().getOrder())
                    .collect(Collectors.toList());

            List<Task> doneTasks = todasTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .collect(Collectors.toList());

            long total = todasTasks.size();
            long concluidas = doneTasks.size();
            int progresso = total > 0 ? (int) ((concluidas * 100) / total) : 0;

            long urgentes = todasTasks.stream()
                    .filter(t -> t.getPriority() == TaskPriority.HIGH && t.getStatus() != TaskStatus.DONE)
                    .count();

            long venceHoje = todasTasks.stream()
                    .filter(Task::isVenceHoje)
                    .count();

            model.addAttribute("nomeUsuario", usuario.getNome());
            model.addAttribute("todoTasks", todoTasks);
            model.addAttribute("doingTasks", doingTasks);
            model.addAttribute("doneTasks", doneTasks);
            model.addAttribute("listaTags", tagService.listarTagsDoUsuario(email));
            model.addAttribute("progresso", progresso);
            model.addAttribute("total", total);
            model.addAttribute("concluidas", concluidas);
            model.addAttribute("urgentes", urgentes);
            model.addAttribute("venceHoje", venceHoje);
            model.addAttribute("currentPage", "kanban");

            return "kanban";
        } catch (Exception e) {
            log.error("Erro ao carregar kanban: {}", e.getMessage());
            return "kanban";
        }
    }

    @PostMapping("/tarefas/mover-dia/{id}")
    @ResponseBody
    public ResponseEntity<?> moverTaskDia(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Task não encontrada"));

            int diaSemana = request.get("diaSemana");
            LocalDate novaData = LocalDate.now().with(java.time.DayOfWeek.of(diaSemana));
            task.setDueDate(novaData);
            taskRepository.save(task);

            return ResponseEntity.ok(Map.of("success", true, "message", "Task movida com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}