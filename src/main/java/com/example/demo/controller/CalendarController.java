package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.model.TaskStatus;
import com.example.demo.model.Usuario;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;
    private final TagService tagService;

    @GetMapping("/calendario")
    public String calendario(
            Authentication authentication, Model model,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        String email = authentication.getName();
        log.info(" Acessando calendário - Mês: {}, Ano: {}", mes, ano);

        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            LocalDate hoje = LocalDate.now();
            int mesAtual = (mes != null) ? mes : hoje.getMonthValue();
            int anoAtual = (ano != null) ? ano : hoje.getYear();

            LocalDate primeiroDia = LocalDate.of(anoAtual, mesAtual, 1);
            List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());

            List<Map<String, Object>> diasDoCalendario = new ArrayList<>();

            int primeiroDiaSemana = primeiroDia.getDayOfWeek().getValue();
            for (int i = 1; i < primeiroDiaSemana; i++) {
                Map<String, Object> diaVazio = new HashMap<>();
                diaVazio.put("vazio", true);
                diaVazio.put("dia", 0);
                diasDoCalendario.add(diaVazio);
            }

            int diasNoMes = primeiroDia.lengthOfMonth();
            for (int dia = 1; dia <= diasNoMes; dia++) {
                LocalDate dataAtual = LocalDate.of(anoAtual, mesAtual, dia);
                List<Task> tasksDoDia = todasTasks.stream()
                        .filter(t -> t.getDueDate() != null && t.getDueDate().equals(dataAtual))
                        .collect(Collectors.toList());

                Map<String, Object> diaInfo = new HashMap<>();
                diaInfo.put("vazio", false);
                diaInfo.put("dia", dia);
                diaInfo.put("tasks", tasksDoDia);
                diaInfo.put("isToday", dataAtual.equals(hoje));
                diaInfo.put("data", dataAtual);
                diasDoCalendario.add(diaInfo);

                if (!tasksDoDia.isEmpty()) {
                    log.info("📅 Dia {}: {} tasks", dia, tasksDoDia.size());
                }
            }

            String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

            model.addAttribute("nomeUsuario", usuario.getNome());
            model.addAttribute("mesAtual", mesAtual);
            model.addAttribute("anoAtual", anoAtual);
            model.addAttribute("nomeMes", meses[mesAtual - 1]);
            model.addAttribute("diasDoCalendario", diasDoCalendario);
            model.addAttribute("hoje", hoje);
            model.addAttribute("listaTags", tagService.listarTagsDoUsuario(email));
            model.addAttribute("currentPage", "calendario");

            return "calendario";
        } catch (Exception e) {
            log.error("Erro ao carregar calendário: {}", e.getMessage());
            return "calendario";
        }
    }

    @GetMapping("/calendario/semana")
    public String calendarioSemana(
            Authentication authentication, Model model,
            @RequestParam(required = false) LocalDate semana) {
        log.info("📅 Acessando calendário SEMANAL - Data: {}", semana);
        String email = authentication.getName();

        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            LocalDate dataBase = (semana != null) ? semana : LocalDate.now();
            LocalDate inicioSemana = dataBase.with(DayOfWeek.MONDAY);
            LocalDate fimSemana = inicioSemana.plusDays(6);

            List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());
            List<List<Task>> tasksPorDia = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                tasksPorDia.add(new ArrayList<>());
            }

            LocalDate dataAtual = inicioSemana;
            for (int i = 0; i < 7; i++) {
                LocalDate dia = dataAtual.plusDays(i);
                List<Task> tasksDoDia = todasTasks.stream()
                        .filter(t -> t.getDueDate() != null && t.getDueDate().equals(dia))
                        .collect(Collectors.toList());
                tasksPorDia.set(i, tasksDoDia);
                log.info(" Dia {} ({}): {} tasks", i + 1, dia, tasksDoDia.size());
            }

            String[] diasSemana = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};

            model.addAttribute("nomeUsuario", usuario.getNome());
            model.addAttribute("inicioSemana", inicioSemana);
            model.addAttribute("fimSemana", fimSemana);
            model.addAttribute("tasksPorDia", tasksPorDia);
            model.addAttribute("diasSemana", diasSemana);
            model.addAttribute("hoje", LocalDate.now());
            model.addAttribute("currentPage", "calendario");

            return "calendario-semana";
        } catch (Exception e) {
            log.error("Erro ao carregar calendário semanal: {}", e.getMessage());
            return "calendario-semana";
        }
    }

    @GetMapping("/calendario/dia")
    public String calendarioDia(
            Authentication authentication, Model model,
            @RequestParam(required = false) LocalDate dia) {
        String email = authentication.getName();

        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            LocalDate dataSelecionada = (dia != null) ? dia : LocalDate.now();
            List<Task> todasTasks = taskRepository.findByUsuarioIdWithTags(usuario.getId());

            List<Task> tasksDia = todasTasks.stream()
                    .filter(t -> t.getDueDate() != null)
                    .filter(t -> t.getDueDate().equals(dataSelecionada))
                    .sorted((a, b) -> {
                        if (a.getPriority() != b.getPriority()) {
                            return b.getPriority().getOrder() - a.getPriority().getOrder();
                        }
                        return a.getTitle().compareTo(b.getTitle());
                    })
                    .collect(Collectors.toList());

            long total = tasksDia.size();
            long concluidas = tasksDia.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
            long pendentes = tasksDia.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
            long emAndamento = tasksDia.stream().filter(t -> t.getStatus() == TaskStatus.DOING).count();

            model.addAttribute("nomeUsuario", usuario.getNome());
            model.addAttribute("dataSelecionada", dataSelecionada);
            model.addAttribute("tasksDia", tasksDia);
            model.addAttribute("total", total);
            model.addAttribute("concluidas", concluidas);
            model.addAttribute("pendentes", pendentes);
            model.addAttribute("emAndamento", emAndamento);
            model.addAttribute("hoje", LocalDate.now());
            model.addAttribute("currentPage", "calendario");

            return "calendario-dia";
        } catch (Exception e) {
            log.error("Erro ao carregar calendário diário: {}", e.getMessage());
            return "calendario-dia";
        }
    }
}