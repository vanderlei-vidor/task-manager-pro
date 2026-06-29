package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"name", "usuario_id"},
           name = "uk_tag_name_usuario"
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da tag é obrigatório!")
    @Column(nullable = false)
    private String name;

    // ✅ Cor personalizada (hexadecimal, ex: #ff5733)
    @Column(length = 7)
    @Builder.Default
    private String color = "#6c757d";

    // ✅ Relacionamento com Usuario (dono da tag)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    // ✅ Relacionamento com Tasks (muitos-para-muitos)
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();
}