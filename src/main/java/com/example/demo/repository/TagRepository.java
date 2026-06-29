package com.example.demo.repository;

import com.example.demo.model.Tag;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Busca todas as tags de um usuário
     */
    List<Tag> findByUsuario(Usuario usuario);

    /**
     * Busca todas as tags de um usuário por ID
     */
    List<Tag> findByUsuarioId(Long usuarioId);

    /**
     * Busca uma tag específica do usuário (por nome)
     */
    Optional<Tag> findByUsuarioAndName(Usuario usuario, String name);

    /**
     * Verifica se uma tag existe para o usuário
     */
    boolean existsByUsuarioAndName(Usuario usuario, String name);

    /**
     * Conta tags de um usuário
     */
    long countByUsuario(Usuario usuario);
}