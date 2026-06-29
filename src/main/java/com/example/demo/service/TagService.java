package com.example.demo.service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Tag;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Cria uma nova tag para o usuário
     */
    @Transactional
    public Tag criarTag(String nome, String cor, String emailUsuario) {
        log.info("Criando nova tag '{}' para usuário: {}", nome, emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // Verifica se já existe tag com esse nome
        if (tagRepository.existsByUsuarioAndName(usuario, nome)) {
            throw new IllegalArgumentException("Já existe uma tag com esse nome!");
        }

        Tag tag = Tag.builder()
                .name(nome)
                .color(cor != null ? cor : "#6c757d")
                .usuario(usuario)
                .build();

        Tag tagSalva = tagRepository.save(tag);
        log.info("Tag criada com sucesso! ID: {}", tagSalva.getId());

        return tagSalva;
    }

    /**
     * Lista todas as tags do usuário
     */
    @Transactional(readOnly = true)
    public List<Tag> listarTagsDoUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return tagRepository.findByUsuario(usuario);
    }

    /**
     * Deleta uma tag
     */
    @Transactional
    public void deletarTag(Long tagId, String emailUsuario) {
        log.info("Deletando tag ID: {} do usuário: {}", tagId, emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag não encontrada"));

        if (!tag.getUsuario().getId().equals(usuario.getId())) {
            throw new NotFoundException("Tag não pertence a você");
        }

        tagRepository.delete(tag);
        log.info("Tag {} deletada com sucesso!", tagId);
    }
}