package com.example.demo.service;

import com.example.demo.dto.ReportDTO;
import com.example.demo.model.Task;
import com.example.demo.model.TaskPriority;
import com.example.demo.model.TaskStatus;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Gera relatório para um período específico
     */
    @Transactional(readOnly = true)
    public ReportDTO generateReport(String email, int days) {
        log.info(" Gerando relatório de {} dias para {}", days, email);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Buscar todas as tasks do usuário
        List<Task> allTasks = taskRepository.findByUsuarioIdWithTags(
                usuarioRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
                        .getId()
        );

        // Filtrar tasks do período
        List<Task> periodTasks = allTasks.stream()
                .filter(t -> t.getDueDate() != null 
                        && !t.getDueDate().isBefore(startDate) 
                        && !t.getDueDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Calcular métricas
        long total = periodTasks.size();
        long completed = periodTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long pending = periodTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgress = periodTasks.stream().filter(t -> t.getStatus() == TaskStatus.DOING).count();
        long overdue = periodTasks.stream().filter(Task::isAtrasada).count();

        double completionRate = total > 0 ? (completed * 100.0) / total : 0;
        double tasksPerDay = days > 0 ? (double) total / days : 0;

        // Tasks por status
        Map<String, Long> byStatus = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.counting()
                ));

        // Tasks por prioridade
        Map<String, Long> byPriority = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority().getDisplayName(),
                        Collectors.counting()
                ));

        // Tasks por dia da semana
        Map<String, Long> byDayOfWeek = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDueDate().getDayOfWeek().toString(),
                        Collectors.counting()
                ));

        // Tasks por data (para heatmap)
        Map<LocalDate, Long> byDate = periodTasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getDueDate,
                        Collectors.counting()
                ));

        // Tasks por tag
        Map<String, Long> byTag = new HashMap<>();
        periodTasks.forEach(task -> {
            if (task.getTags() != null) {
                task.getTags().forEach(tag -> {
                    byTag.merge(tag.getName(), 1L, Long::sum);
                });
            }
        });

        // Melhor e pior dia
        String bestDay = byDayOfWeek.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> translateDay(e.getKey()))
                .orElse("N/A");

        String worstDay = byDayOfWeek.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(e -> translateDay(e.getKey()))
                .orElse("N/A");

        // Gerar insights
        List<String> insights = generateInsights(total, completed, completionRate, tasksPerDay, overdue, bestDay);

        // Resumo semanal/mensal
        String weeklySummary = generateWeeklySummary(completed, total);
        String monthlySummary = generateMonthlySummary(completionRate, tasksPerDay);

        return ReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .days(days)
                .totalTasks(total)
                .completedTasks(completed)
                .pendingTasks(pending)
                .inProgressTasks(inProgress)
                .overdueTasks(overdue)
                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                .tasksPerDay(Math.round(tasksPerDay * 10.0) / 10.0)
                .bestDay(bestDay)
                .worstDay(worstDay)
                .productivityTrend(0.0) // Calcular comparando períodos
                .tasksByStatus(byStatus)
                .tasksByPriority(byPriority)
                .tasksByDayOfWeek(byDayOfWeek)
                .tasksByDate(byDate)
                .tasksByTag(byTag)
                .insights(insights)
                .weeklySummary(weeklySummary)
                .monthlySummary(monthlySummary)
                .build();
    }

    private String translateDay(String day) {
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

    private List<String> generateInsights(long total, long completed, double rate, double perDay, long overdue, String bestDay) {
        List<String> insights = new ArrayList<>();
        
        if (rate >= 80) {
            insights.add("🏆 Taxa de conclusão excelente (" + Math.round(rate) + "%)");
        } else if (rate >= 50) {
            insights.add(" Boa taxa de conclusão (" + Math.round(rate) + "%)");
        } else {
            insights.add("⚠️ Taxa de conclusão pode melhorar (" + Math.round(rate) + "%)");
        }

        if (perDay >= 2) {
            insights.add(" Alta produtividade: " + perDay + " tarefas/dia");
        } else if (perDay >= 1) {
            insights.add(" Produtividade estável: " + perDay + " tarefas/dia");
        }

        if (overdue > 0) {
            insights.add("⏰ " + overdue + " tarefa(s) atrasada(s) - atenção!");
        }

        insights.add(" Seu melhor dia: " + bestDay);

        return insights;
    }

    private String generateWeeklySummary(long completed, long total) {
        if (completed == 0) return "Nenhuma tarefa concluída ainda. Bora começar!";
        if (completed == total) return "Todas as tarefas concluídas! Parabéns!";
        return completed + " de " + total + " tarefas concluídas. Continue assim!";
    }

    private String generateMonthlySummary(double rate, double perDay) {
        return String.format("Taxa de %.1f%% com média de %.1f tarefas/dia", rate, perDay);
    }
}