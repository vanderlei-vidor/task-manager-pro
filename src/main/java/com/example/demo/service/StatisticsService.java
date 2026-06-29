package com.example.demo.service;

import com.example.demo.dto.StatisticsDTO;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para cálculos de estatísticas
 * Reutilizável em Dashboard, Kanban, Reports
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Calcula estatísticas completas do usuário
     */
    @Transactional(readOnly = true)
    public StatisticsDTO calculateStatistics(String email) {
        log.info("📊 Calculando estatísticas para: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());

        return buildStatistics(todasTasks);
    }

    /**
     * Calcula estatísticas para um período específico
     */
    @Transactional(readOnly = true)
    public StatisticsDTO calculateStatisticsForPeriod(String email, LocalDate inicio, LocalDate fim) {
        log.info("📊 Calculando estatísticas do período {} até {}", inicio, fim);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());

        List<Task> tasksDoPeriodo = todasTasks.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> !t.getDueDate().isBefore(inicio) && !t.getDueDate().isAfter(fim))
                .collect(Collectors.toList());

        return buildStatistics(tasksDoPeriodo);
    }

    /**
     * Monta o objeto de estatísticas a partir da lista de tasks
     */
    private StatisticsDTO buildStatistics(List<Task> tasks) {
        long total = tasks.size();
        long concluidas = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long pendentes = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long emAndamento = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DOING).count();
        long atrasadas = tasks.stream().filter(Task::isAtrasada).count();

        // Por prioridade
        long alta = tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();
        long media = tasks.stream().filter(t -> t.getPriority() == TaskPriority.MEDIUM).count();
        long baixa = tasks.stream().filter(t -> t.getPriority() == TaskPriority.LOW).count();

        // Taxa de conclusão
        double taxaConclusao = total > 0 ? (concluidas * 100.0) / total : 0;

        // Melhor dia da semana
        Map<String, Long> porDiaSemana = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getDueDate().getDayOfWeek().toString(),
                        Collectors.counting()
                ));

        String melhorDia = porDiaSemana.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> traduzirDia(e.getKey()))
                .orElse("N/A");

        // Produtividade (tasks/dia)
        long diasComTasks = tasks.stream()
                .map(Task::getDueDate)
                .distinct()
                .count();
        double produtividade = diasComTasks > 0 ? (double) total / diasComTasks : 0;

        return StatisticsDTO.builder()
                .total(total)
                .concluidas(concluidas)
                .pendentes(pendentes)
                .emAndamento(emAndamento)
                .atrasadas(atrasadas)
                .altaPrioridade(alta)
                .mediaPrioridade(media)
                .baixaPrioridade(baixa)
                .taxaConclusao(Math.round(taxaConclusao * 10.0) / 10.0)
                .melhorDia(melhorDia)
                .produtividade(Math.round(produtividade * 10.0) / 10.0)
                .build();
    }

    private String traduzirDia(String day) {
        return switch (day) {
            case "MONDAY" -> "Segunda";
            case "TUESDAY" -> "Terça";
            case "WEDNESDAY" -> "Quarta";
            case "THURSDAY" -> "Quinta";
            case "FRIDAY" -> "Sexta";
            case "SATURDAY" -> "Sábado";
            case "SUNDAY" -> "Domingo";
            default -> day;
        };
    }
}