package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório!")
    @Column(nullable = false)
    private String title;

    private String description;

    @NotNull(message = "Status é obrigatório!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    // ✅ NOVO: Prioridade da task
    @NotNull(message = "Prioridade é obrigatória!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Column(updatable = false)
    private LocalDateTime dataCriacao;

    // ✅ Relacionamento com Usuario (muitos-para-um)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    // ✅ NOVO: Relacionamento com Tags (muitos-para-muitos)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        if (this.status == null) {
            this.status = TaskStatus.TODO;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }
    }

    // ========================================
    // MÉTODOS HELPER
    // ========================================

    public String getDataCriacaoFormatada() {
        if (this.dataCriacao == null) {
            return "Sem data";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return this.dataCriacao.format(formatter);
    }

    public String getDueDateFormatada() {
        if (this.dueDate == null) {
            return "Sem vencimento";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return this.dueDate.format(formatter);
    }

    /**
     * Calcula dias restantes até o vencimento
     */
    public Long getDiasRestantes() {
        if (this.dueDate == null || this.status == TaskStatus.DONE) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
    }

    public boolean isAtrasada() {
        return this.dueDate != null 
            && this.dueDate.isBefore(LocalDate.now()) 
            && this.status != TaskStatus.DONE;
    }

    public boolean isConcluida() {
        return this.status == TaskStatus.DONE;
    }

    /**
     * Verifica se vence hoje
     */
    public boolean isVenceHoje() {
        return this.dueDate != null 
            && this.dueDate.equals(LocalDate.now()) 
            && this.status != TaskStatus.DONE;
    }

    /**
     * Verifica se vence nos próximos 3 dias
     */
    public boolean isVenceEmBreve() {
        if (this.dueDate == null || this.status == TaskStatus.DONE) {
            return false;
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
        return dias >= 0 && dias <= 3;
    }

    // ========================================
    // MÉTODOS HELPER PARA TAGS
    // ========================================

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getTasks().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getTasks().remove(this);
    }

    public void clearTags() {
        new HashSet<>(this.tags).forEach(this::removeTag);
    }
}