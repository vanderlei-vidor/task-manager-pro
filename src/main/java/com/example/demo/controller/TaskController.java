package com.example.demo.controller;

import com.example.demo.dto.TaskDTO;
import com.example.demo.dto.TaskStatusUpdateDTO;
import com.example.demo.model.TaskStatus;
import com.example.demo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ✅ CRIAR
    @PostMapping
    public ResponseEntity<TaskDTO> criar(@Valid @RequestBody TaskDTO dto, 
                                         Authentication authentication) {
        TaskDTO taskCriada = taskService.criarTask(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCriada);
    }

    // ✅ LISTAR
    @GetMapping
    public ResponseEntity<List<TaskDTO>> listar(Authentication authentication) {
        List<TaskDTO> tasks = taskService.listarTasksDoUsuario(authentication.getName());
        return ResponseEntity.ok(tasks);
    }

    // ✅ BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> buscarPorId(@PathVariable Long id, 
                                               Authentication authentication) {
        TaskDTO task = taskService.buscarTaskPorId(id, authentication.getName());
        return ResponseEntity.ok(task);
    }

    // ✅ ATUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> atualizar(@PathVariable Long id,
                                             @Valid @RequestBody TaskDTO dto,
                                             Authentication authentication) {
        TaskDTO taskAtualizada = taskService.atualizarTask(id, dto, authentication.getName());
        return ResponseEntity.ok(taskAtualizada);
    }

    // ✅ ATUALIZAR STATUS (Query Param - mantém para compatibilidade)
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDTO> atualizarStatus(@PathVariable Long id,
                                                   @RequestParam TaskStatus status,
                                                   Authentication authentication) {
        TaskDTO taskAtualizada = taskService.atualizarStatus(id, status, authentication.getName());
        return ResponseEntity.ok(taskAtualizada);
    }

    // 🆕 ATUALIZAR STATUS VIA BODY (Para Kanban Drag & Drop)
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDTO> atualizarStatusDnd(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateDTO dto,
            Authentication authentication) {
        
        TaskDTO taskAtualizada = taskService.atualizarStatus(id, dto.getStatus(), authentication.getName());
        return ResponseEntity.ok(taskAtualizada);
    }

    // ✅ DELETAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, 
                                        Authentication authentication) {
        taskService.deletarTask(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ✅ DASHBOARD
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        Map<String, Object> stats = taskService.getEstatisticas(authentication.getName());
        return ResponseEntity.ok(stats);
    }
}