package com.example.demo.service;

import com.example.demo.dto.TaskDTO;
import com.example.demo.model.Task;
import com.example.demo.model.TaskPriority;
import com.example.demo.model.TaskStatus;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;
    private final TaskService taskService;
    private final TagService tagService;

    /**
     * Carrega todos os dados necessários para o dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> loadDashboardData(String email) {
        log.info("📊 Carregando dados do dashboard para: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Task> listaCompleta = taskRepository.findByUsuarioIdWithTags(usuario.getId());
        List<TaskDTO> tasks = listaCompleta.stream()
                .map(taskService::converterParaDTO)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("usuario", usuario);
        data.put("listaDeTarefas", listaCompleta);
        data.put("tasks", tasks);

        // Estatísticas
        data.put("total", tasks.size());
        data.put("qtdPendentes", tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count());
        data.put("qtdFazendo", tasks.stream().filter(t -> t.getStatus() == TaskStatus.DOING).count());
        data.put("qtdConcluidas", tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count());
        data.put("qtdAlta", tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count());
        data.put("qtdMedia", tasks.stream().filter(t -> t.getPriority() == TaskPriority.MEDIUM).count());
        data.put("qtdBaixa", tasks.stream().filter(t -> t.getPriority() == TaskPriority.LOW).count());

        // Tags
        data.put("listaTags", tagService.listarTagsDoUsuario(email));

        return data;
    }
}