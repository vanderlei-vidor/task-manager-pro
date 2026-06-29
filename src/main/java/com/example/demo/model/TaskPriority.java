package com.example.demo.model;

import lombok.Getter;

/**
 * Prioridade de uma Task
 * 
 * Representa a urgência da tarefa
 */
@Getter
public enum TaskPriority {
    
    LOW("Baixa", "#28a745", 1),       // Verde
    MEDIUM("Média", "#ffc107", 2),    // Amarelo
    HIGH("Alta", "#dc3545", 3);       // Vermelho
    
    private final String displayName;
    private final String color;       // Cor pra UI
    private final int order;          // Ordem pra ordenação
    
    TaskPriority(String displayName, String color, int order) {
        this.displayName = displayName;
        this.color = color;
        this.order = order;
    }
    
    /**
     * Retorna o ícone Bootstrap pra essa prioridade
     */
    public String getIcon() {
        return switch (this) {
            case LOW -> "bi-arrow-down-circle";
            case MEDIUM -> "bi-dash-circle";
            case HIGH -> "bi-exclamation-circle-fill";
        };
    }
    
    /**
     * Retorna a classe CSS Bootstrap pra essa prioridade
     */
    public String getBootstrapClass() {
        return switch (this) {
            case LOW -> "bg-success";
            case MEDIUM -> "bg-warning text-dark";
            case HIGH -> "bg-danger";
        };
    }
}