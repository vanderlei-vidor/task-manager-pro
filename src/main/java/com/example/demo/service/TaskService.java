package com.example.demo.service;

import com.example.demo.dto.TaskDTO;
import com.example.demo.exception.BusinessRuleViolationException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.model.Tag;
import com.example.demo.model.Task;
import com.example.demo.model.TaskPriority;
import com.example.demo.model.TaskStatus;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor  // ✅ ÚNICA anotação de injeção necessária!
public class TaskService {

    // ✅ TODOS os campos são final (obrigatório com @RequiredArgsConstructor)
    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;  // ✅ Mapper injetado

    // ========================================
    // CRIAR TASK
    // ========================================

    /**
     * Cria uma nova task para o usuário autenticado
     */
    @Transactional
    public TaskDTO criarTask(TaskDTO dto, String emailUsuario) {
        log.info("Criando nova task para usuário: {}", emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO)
                .priority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM)
                .dueDate(dto.getDueDate())
                .usuario(usuario)
                .build();

        // Adiciona tags se fornecidas
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = tagRepository.findAllById(dto.getTagIds())
                    .stream()
                    .filter(t -> t.getUsuario().getId().equals(usuario.getId()))
                    .collect(Collectors.toSet());
            task.setTags(tags);
        }

        Task taskSalva = taskRepository.save(task);
        log.info("Task criada com sucesso! ID: {}", taskSalva.getId());

        return converterParaDTO(taskSalva);
    }

    // ========================================
    // LISTAR TASKS DO USUÁRIO
    // ========================================

    /**
     * Lista todas as tasks do usuário autenticado
     * ✅ Usa método que carrega tags junto (evita LazyInitializationException)
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> listarTasksDoUsuario(String emailUsuario) {
        log.debug("Listando tasks do usuário: {}", emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // ✅ CORRIGIDO: Usa método que carrega tags
        return taskRepository.findByUsuarioIdWithTags(usuario.getId())
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // LISTAR TASKS PAGINADO (API REST)
    // ========================================

    /**
     * Lista as tasks do usuário de forma paginada.
     * Usa countQuery explícito (DISTINCT + JOIN FETCH não deriva count automaticamente).
     *
     * @param emailUsuario email do usuário autenticado
     * @param pageable     parâmetros de paginação/ordenação (page, size, sort)
     * @return Page<TaskDTO> com content, totalElements, totalPages, etc.
     */
    @Transactional(readOnly = true)
    public Page<TaskDTO> listarTasksPaginado(String emailUsuario, Pageable pageable) {
        log.debug("Listando tasks paginado do usuário: {} | pageable={}", emailUsuario, pageable);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Page<Task> page = taskRepository.findByUsuarioIdWithTagsPaginado(usuario.getId(), pageable);

        // ✅ page.map converte Page<Task> → Page<TaskDTO> usando o MapStruct mapper
        return page.map(taskMapper::toDTO);
    }

    // ========================================
    // BUSCAR TASK POR ID (COM SEGURANÇA!)
    // ========================================

    /**
     * Busca uma task específica - GARANTE que pertence ao usuário!
     */
    @Transactional(readOnly = true)
    public TaskDTO buscarTaskPorId(Long taskId, String emailUsuario) {
        log.debug("Buscando task ID: {} do usuário: {}", taskId, emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Task task = taskRepository.findByIdAndUsuarioId(taskId, usuario.getId())
                .orElseThrow(() -> new NotFoundException("Task não encontrada ou não pertence a você"));

        return converterParaDTO(task);
    }

    // ========================================
    // ATUALIZAR TASK
    // ========================================

    /**
     * Atualiza uma task existente
     */
    @Transactional
    public TaskDTO atualizarTask(Long taskId, TaskDTO dto, String emailUsuario) {
        log.info("Atualizando task ID: {} do usuário: {}", taskId, emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Task task = taskRepository.findByIdAndUsuarioId(taskId, usuario.getId())
                .orElseThrow(() -> new NotFoundException("Task não encontrada ou não pertence a você"));

        // Validação: Se a task está DONE, não pode mais editar!
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessRuleViolationException(
                    BusinessRuleViolationException.ViolationType.INVALID_STATUS_TRANSITION,
                    "Não é possível editar uma task já concluída!");
        }

        // Atualiza os campos
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }

        // Atualiza tags se fornecidas
        if (dto.getTagIds() != null) {
            task.clearTags();
            if (!dto.getTagIds().isEmpty()) {
                Set<Tag> tags = tagRepository.findAllById(dto.getTagIds())
                        .stream()
                        .filter(t -> t.getUsuario().getId().equals(usuario.getId()))
                        .collect(Collectors.toSet());
                task.setTags(tags);
            }
        }

        Task taskAtualizada = taskRepository.save(task);
        log.info("Task atualizada com sucesso! ID: {}", taskAtualizada.getId());

        return converterParaDTO(taskAtualizada);
    }

    // ========================================
    // ATUALIZAR STATUS (COM VALIDAÇÃO DE TRANSIÇÃO!)
    // ========================================

    /**
     * Atualiza o status da task - VALIDA a transição!
     * Fluxo permitido: TODO → DOING → DONE
     */
    @Transactional
    public TaskDTO atualizarStatus(Long taskId, TaskStatus novoStatus, String emailUsuario) {
        log.info("Atualizando status da task ID: {} para: {}", taskId, novoStatus);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Task task = taskRepository.findByIdAndUsuarioId(taskId, usuario.getId())
                .orElseThrow(() -> new NotFoundException("Task não encontrada ou não pertence a você"));

        TaskStatus statusAntigo = task.getStatus();
        
        // Validação de transição
        validarTransicaoDeStatus(statusAntigo, novoStatus);

        task.setStatus(novoStatus);
        Task taskAtualizada = taskRepository.save(task);

        log.info("Status da task {} alterado de {} para {}",
                taskId, statusAntigo, novoStatus);

        return converterParaDTO(taskAtualizada);
    }

    /**
     * Valida se a transição de status é permitida
     */
    private void validarTransicaoDeStatus(TaskStatus statusAtual, TaskStatus novoStatus) {
        if (statusAtual == novoStatus) {
            return;
        }

        boolean transicaoValida = switch (statusAtual) {
            case TODO -> novoStatus == TaskStatus.DOING;
            case DOING -> novoStatus == TaskStatus.DONE || novoStatus == TaskStatus.TODO;
            case DONE -> false;
        };

        if (!transicaoValida) {
            throw new BusinessRuleViolationException(
                    BusinessRuleViolationException.ViolationType.INVALID_STATUS_TRANSITION,
                    String.format("Transição inválida: %s → %s", statusAtual, novoStatus));
        }
    }

    // ========================================
    // DELETAR TASK
    // ========================================

    /**
     * Deleta uma task - GARANTE que pertence ao usuário!
     */
    @Transactional
    public void deletarTask(Long taskId, String emailUsuario) {
        log.info("Deletando task ID: {} do usuário: {}", taskId, emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Task task = taskRepository.findByIdAndUsuarioId(taskId, usuario.getId())
                .orElseThrow(() -> new NotFoundException("Task não encontrada ou não pertence a você"));

        taskRepository.delete(task);
        log.info("Task {} deletada com sucesso!", taskId);
    }

    // ========================================
    // DASHBOARD - ESTATÍSTICAS
    // ========================================

    /**
     * Retorna estatísticas do usuário para o dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticas(String emailUsuario) {
        log.debug("Gerando estatísticas para usuário: {}", emailUsuario);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTodo", taskRepository.countByUsuarioAndStatus(usuario, TaskStatus.TODO));
        stats.put("totalDoing", taskRepository.countByUsuarioAndStatus(usuario, TaskStatus.DOING));
        stats.put("totalDone", taskRepository.countByUsuarioAndStatus(usuario, TaskStatus.DONE));
        stats.put("totalGeral", taskRepository.countByUsuarioId(usuario.getId()));

        List<Task> atrasadas = taskRepository.findByUsuarioAndStatusNotAndDueDateBefore(
                usuario, TaskStatus.DONE, LocalDate.now());
        stats.put("totalAtrasadas", atrasadas.size());

        List<Task> proximas = taskRepository.findTasksProximasDoVencimento(
                usuario, LocalDate.now(), LocalDate.now().plusDays(7));
        stats.put("totalProximasVencimento", proximas.size());

        return stats;
    }

    // ========================================
    // CONVERSOR: Entity → DTO (USANDO MAPSTRUCT!)
    // ========================================

    /**
     * Converte Task (entity) para TaskDTO
     * ✅ Usa MapStruct para conversão automática!
     * ✅ NUNCA expõe a entity diretamente na API!
     */
    public TaskDTO converterParaDTO(Task task) {
        return taskMapper.toDTO(task);  // ✅ MapStruct faz tudo automaticamente!
    }

    /**
     * Converte lista de Tasks para lista de TaskDTOs
     */
    public List<TaskDTO> converterParaDTOList(List<Task> tasks) {
        return taskMapper.toDTOList(tasks);  // ✅ MapStruct converte lista inteira!
    }
}