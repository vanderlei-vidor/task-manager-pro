package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String nome;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Task> tarefas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ========================================
    // 🚀 NOVO: MAPEAMENTO DOS REFRESH TOKENS PARA DELETAR EM CASCATA
    // ========================================
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>(); // Altere o tipo se sua classe tiver outro nome (ex: Token)

    // ========================================
    // MÉTODOS HELPER
    // ========================================

    public void addTask(Task task) {
        tarefas.add(task);
        task.setUsuario(this);
    }

    public void removeTask(Task task) {
        tarefas.remove(task);
        task.setUsuario(null);
    }

    public int getQuantidadeTasks() {
        return tarefas != null ? tarefas.size() : 0;
    }

    public boolean temTasks() {
        return tarefas != null && !tarefas.isEmpty();
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.setUsuario(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.setUsuario(null);
    }
}