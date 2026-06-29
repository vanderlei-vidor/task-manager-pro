package com.example.demo.model;

import lombok.Getter;

/**
 * Status de uma Task no sistema
 * 
 * Representa o fluxo: TODO → DOING → DONE
 * 
 * @author Vanderlei (Task Manager Pro)
 */
@Getter
public enum TaskStatus {
    
    TODO("A Fazer", "Tarefa ainda não iniciada"),
    DOING("Em Andamento", "Tarefa sendo executada"),
    DONE("Concluída", "Tarefa finalizada com sucesso");
    
    private final String displayName;
    private final String description;
    
    TaskStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Retorna o próximo status no fluxo
     * TODO → DOING → DONE
     */
    public TaskStatus nextStatus() {
        return switch (this) {
            case TODO -> DOING;
            case DOING -> DONE;
            case DONE -> throw new IllegalStateException("Tarefa já concluída!");
        };
    }
    
    /**
     * Verifica se pode transicionar para outro status
     */
    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case TODO -> target == DOING;
            case DOING -> target == DONE || target == TODO; // Pode voltar!
            case DONE -> false; // Concluída não volta
        };
    }
    
    /**
     * Verifica se a tarefa está finalizada
     */
    public boolean isFinalized() {
        return this == DONE;
    }
    
    /**
     * Verifica se a tarefa está ativa (não concluída)
     */
    public boolean isActive() {
        return this == TODO || this == DOING;
    }
}