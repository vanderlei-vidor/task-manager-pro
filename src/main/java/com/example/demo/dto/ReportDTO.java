package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    // Período do relatório
    private LocalDate startDate;
    private LocalDate endDate;
    private int days;

    // Métricas gerais
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long overdueTasks;
    private double completionRate;

    // Produtividade
    private double tasksPerDay;
    private String bestDay;
    private String worstDay;
    private double productivityTrend;

    // Dados para gráficos
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> tasksByDayOfWeek;
    private Map<LocalDate, Long> tasksByDate;
    private Map<String, Long> tasksByTag;

    // Insights
    private List<String> insights;
    private String weeklySummary;
    private String monthlySummary;
}