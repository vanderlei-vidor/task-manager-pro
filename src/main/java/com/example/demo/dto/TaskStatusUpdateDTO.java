package com.example.demo.dto;

import com.example.demo.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO específico para atualização de status via Kanban Drag & Drop.
 * 
 * 💡 Por que um DTO separado?
 * - O drag-and-drop só precisa enviar o novo status
 * - Não faz sentido validar título, descrição, etc.
 * - Payload menor = performance melhor
 * - Princípio da Responsabilidade Única (SRP)
 * 
 * Exemplo de request:
 * PUT /api/tasks/123/status
 * {
 *   "status": "DOING"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateDTO {

    @NotNull(message = "Status é obrigatório!")
    private TaskStatus status;
}