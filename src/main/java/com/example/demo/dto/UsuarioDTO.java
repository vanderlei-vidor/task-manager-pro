package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    @NotBlank(message = "Nome é obrigatório!")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório!")
    @Email(message = "E-mail deve ser um formato válido!")
    private String email;

    @NotBlank(message = "Senha é obrigatória e deve ter no mínimo 6 caracteres!")
    private String senha;

    public boolean isValid() {
        return !getNome().isBlank() &&
               !getEmail().isBlank() &&
               getSenha() != null &&
               getSenha().length() >= 6;
    }

    public boolean hasMinimumPasswordStrength(String senha) {
        return senha != null && senha.length() >= 8 &&
               (senha.matches(".*[A-Z].*") || senha.matches(".*[a-z].*")) &&
               senha.matches(".*\\d.*");
    }
}