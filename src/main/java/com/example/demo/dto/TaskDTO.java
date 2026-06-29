package com.example.demo.dto;

import com.example.demo.model.TaskPriority;
import com.example.demo.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Título é obrigatório!")
    private String title;

    private String description;

    @NotNull(message = "Status é obrigatório!")
    private TaskStatus status;

     @NotNull(message = "Prioridade é obrigatória!")
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    private LocalDateTime dataCriacao;

    // ✅ NOVO: IDs das tags (pra enviar no request)
    private Set<Long> tagIds;

    // ✅ NOVO: Nomes das tags (pra exibir na resposta)
    private Set<String> tagNames;

    // Campos calculados
    private Boolean atrasada;
    private Boolean concluida;
    private Boolean venceHoje;
    private Boolean venceEmBreve;
    private Long diasRestantes;
}