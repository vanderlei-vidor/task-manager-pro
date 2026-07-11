package com.example.demo.config;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializador de dados de DESENVOLVIMENTO.
 *
 * ⚠️ SÓ RODA EM PERFIL "dev" — nunca em produção.
 * Cria um usuário admin de teste para facilitar o desenvolvimento local.
 *
 * Em produção, o administrador deve ser criado via migration Flyway/Liquibase
 * ou script controlado — nunca via seed automático com senha fraca.
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Bean
    public CommandLineRunner seedDevAdmin() {
        return args -> {
            String adminEmail = "admin@teste.com";

            repository.findByEmail(adminEmail).ifPresentOrElse(
                admin -> {
                    // Garante senha BCrypt se o usuário já existir
                    admin.setSenha(encoder.encode("123"));
                    repository.save(admin);
                    log.info("✅ [DEV] Senha do {} atualizada para BCrypt", adminEmail);
                },
                () -> {
                    Usuario admin = new Usuario();
                    admin.setNome("Vanderlei");
                    admin.setEmail(adminEmail);
                    admin.setSenha(encoder.encode("123"));
                    repository.save(admin);
                    log.info("✅ [DEV] Usuário {} criado (senha: 123)", adminEmail);
                }
            );
        };
    }
}
