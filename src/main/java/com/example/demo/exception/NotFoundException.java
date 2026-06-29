package com.example.demo.exception;

import com.example.demo.model.TaskStatus;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    @FunctionalInterface
    public interface TaskNotFoundReason {
        String apply(TaskStatus status);
    }

    private final TaskNotFoundReason reason;

    // ✅ Construtor com TaskNotFoundReason (para tasks específicas)
    public NotFoundException(TaskNotFoundReason reason) {
        super("Tarefa não encontrada");
        this.reason = reason;
    }

    // ✅ Construtor com String (mensagem customizada) - O QUE O TaskService USA!
    public NotFoundException(String message) {
        super(message);
        this.reason = null;
    }

    // ✅ Construtor com ambos (TaskNotFoundReason + mensagem custom)
    public NotFoundException(TaskNotFoundReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        // Se tem reason (caso antigo), usa a formatação
        if (reason != null) {
            return String.format(
                "Tarefa com status %s não encontrada.",
                reason.apply(null)
            );
        }
        // Senão, retorna a mensagem customizada (caso novo)
        return super.getMessage();
    }
}