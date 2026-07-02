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
import com.example.demo.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("📋 TaskService - Testes de Regras de Negócio")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Usuario usuarioTeste;
    private Usuario outroUsuario;
    private TaskDTO taskDTOTeste;
    private Task taskTeste;

    private static final String EMAIL_TESTE = "teste@example.com";
    private static final String OUTRO_EMAIL = "outro@example.com";
    private static final Long USUARIO_ID = 1L;
    private static final Long OUTRO_USUARIO_ID = 2L;
    private static final Long TASK_ID = 100L;

    @BeforeEach
    void setUp() {
        usuarioTeste = Usuario.builder().id(USUARIO_ID).email(EMAIL_TESTE).nome("Usuário Teste").build();
        outroUsuario = Usuario.builder().id(OUTRO_USUARIO_ID).email(OUTRO_EMAIL).nome("Outro Usuário").build();

        taskDTOTeste = TaskDTO.builder()
                .title("Task de Teste")
                .description("Descrição da task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        taskTeste = Task.builder()
                .id(TASK_ID)
                .title("Task Existente")
                .description("Descrição existente")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5))
                .usuario(usuarioTeste)
                .build();
    }

    @Nested
    @DisplayName("📝 Método: criarTask")
    class CriarTaskTests {

        @Test
        @DisplayName("✅ Deve criar task com dados válidos")
        void deveCriarTaskComDadosValidos() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
                Task t = i.getArgument(0);
                t.setId(TASK_ID);
                return t;
            });
            when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTOTeste);

            TaskDTO resultado = taskService.criarTask(taskDTOTeste, EMAIL_TESTE);

            assertThat(resultado).isNotNull();
            verify(usuarioRepository).findByEmail(EMAIL_TESTE);
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("✅ Deve criar task com status e prioridade default")
        void deveCriarTaskComStatusEPrioridadeDefault() {
            TaskDTO dtoSemStatus = TaskDTO.builder().title("Task sem status").description("Descrição").build();
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
                Task t = i.getArgument(0);
                t.setId(TASK_ID);
                return t;
            });

            taskService.criarTask(dtoSemStatus, EMAIL_TESTE);

            verify(taskRepository).save(
                    argThat(task -> task.getStatus() == TaskStatus.TODO && task.getPriority() == TaskPriority.MEDIUM));
        }

        @Test
        @DisplayName("✅ Deve criar task com tags válidas")
        void deveCriarTaskComTags() {
            Tag tag1 = Tag.builder().id(1L).name("Tag 1").usuario(usuarioTeste).build();
            Tag tag2 = Tag.builder().id(2L).name("Tag 2").usuario(usuarioTeste).build();
            TaskDTO dtoComTags = TaskDTO.builder().title("Task com tags").tagIds(Set.of(1L, 2L)).build();

            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(tagRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(tag1, tag2));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            taskService.criarTask(dtoComTags, EMAIL_TESTE);

            verify(taskRepository).save(argThat(task -> task.getTags() != null && task.getTags().size() == 2));
        }

        @Test
        @DisplayName("🔒 Deve filtrar tags que não pertencem ao usuário autor do recurso")
        void deveFiltrarTagsQueNaoPertencemAoUsuario() {
            Tag tagDoUsuario = Tag.builder().id(1L).name("Minha Tag").usuario(usuarioTeste).build();
            Tag tagDeOutro = Tag.builder().id(2L).name("Tag de Outro").usuario(outroUsuario).build();
            TaskDTO dtoComTags = TaskDTO.builder().title("Task misturada").tagIds(Set.of(1L, 2L)).build();

            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(tagRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(tagDoUsuario, tagDeOutro));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            taskService.criarTask(dtoComTags, EMAIL_TESTE);

            verify(taskRepository).save(argThat(task -> task.getTags() != null && task.getTags().size() == 1
                    && task.getTags().iterator().next().getId().equals(1L)));
        }

        @Test
        @DisplayName("❌ Deve lançar NotFoundException quando usuário não existe")
        void deveLancarNotFoundExceptionQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.criarTask(taskDTOTeste, EMAIL_TESTE))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("📋 Método: listarTasksDoUsuario")
    class ListarTasksDoUsuarioTests {

        @Test
        @DisplayName("✅ Deve listar tasks do usuário com sucesso")
        void deveListarTasksDoUsuarioComSucesso() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByUsuarioIdWithTags(USUARIO_ID)).thenReturn(List.of(taskTeste));

            List<TaskDTO> resultado = taskService.listarTasksDoUsuario(EMAIL_TESTE);

            assertThat(resultado).isNotNull();
            verify(taskRepository).findByUsuarioIdWithTags(USUARIO_ID);
        }

        @Test
        @DisplayName("❌ Deve lançar NotFoundException quando usuário não mapeado")
        void deveLancarNotFoundExceptionQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.listarTasksDoUsuario(EMAIL_TESTE))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("🔍 Método: buscarTaskPorId")
    class BuscarTaskPorIdTests {

        @Test
        @DisplayName("✅ Deve buscar task existente pertencente ao usuário")
        void deveBuscarTaskExistenteDoUsuario() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskTeste));
            when(taskMapper.toDTO(taskTeste)).thenReturn(taskDTOTeste);

            TaskDTO resultado = taskService.buscarTaskPorId(TASK_ID, EMAIL_TESTE);

            assertThat(resultado).isNotNull();
            verify(taskRepository).findByIdAndUsuarioId(TASK_ID, USUARIO_ID);
        }

        @Test
        @DisplayName("❌/🔒 Deve retornar NotFoundException se a task não existe ou pertence a outro tenant")
        void deveLancarNotFoundExceptionQuandoTaskNaoExisteOuForDeOutro() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.buscarTaskPorId(TASK_ID, EMAIL_TESTE))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("✏️ Método: atualizarTask")
    class AtualizarTaskTests {

        @Test
        @DisplayName("✅ Deve atualizar metadados da task com sucesso")
        void deveAtualizarTaskComSucesso() {
            TaskDTO dtoAtualizado = TaskDTO.builder().title("Título Atualizado").priority(TaskPriority.HIGH).build();
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskTeste));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
            // Adicionado para evitar o erro "Expecting actual not to be null"
            when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTOTeste);

            TaskDTO resultado = taskService.atualizarTask(TASK_ID, dtoAtualizado, EMAIL_TESTE);

            assertThat(resultado).isNotNull();
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("❌ Bloqueio de Alteração: Não deve permitir editar uma task com status DONE")
        void deveLancarExcecaoAoTentarEditarTaskDONE() {
            Task taskDone = Task.builder().id(TASK_ID).status(TaskStatus.DONE).usuario(usuarioTeste).build();
            TaskDTO dtoAtualizado = TaskDTO.builder().title("Alteração").build();

            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskDone));

            assertThatThrownBy(() -> taskService.atualizarTask(TASK_ID, dtoAtualizado, EMAIL_TESTE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("invalid_status_transition"); // Ajustado aqui

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("🔄 Método: atualizarStatus")
    class AtualizarStatusTests {

        @Test
        @DisplayName("✅ Fluxo: Deve permitir transição linear válida (TODO → DOING)")
        void devePermitirTransicaoTODOparaDOING() {
            Task taskTodo = Task.builder().id(TASK_ID).status(TaskStatus.TODO).usuario(usuarioTeste).build();
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskTodo));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            taskService.atualizarStatus(TASK_ID, TaskStatus.DOING, EMAIL_TESTE);

            verify(taskRepository).save(argThat(t -> t.getStatus() == TaskStatus.DOING));
        }

        @Test
        @DisplayName("❌ Quebra de Fluxo: Deve rejeitar transição saltando etapas (TODO → DONE)")
        void deveRejeitarTransicaoTODOparaDONE() {
            Task taskTodo = Task.builder().id(TASK_ID).status(TaskStatus.TODO).usuario(usuarioTeste).build();
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskTodo));

            assertThatThrownBy(() -> taskService.atualizarStatus(TASK_ID, TaskStatus.DONE, EMAIL_TESTE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("invalid_status_transition"); // Ajustado aqui

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Estado Imutável: Deve rejeitar qualquer alteração a partir do estado DONE")
        void deveRejeitarQualquerTransicaoAPartirDeDONE() {
            Task taskDone = Task.builder().id(TASK_ID).status(TaskStatus.DONE).usuario(usuarioTeste).build();
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskDone));

            assertThatThrownBy(() -> taskService.atualizarStatus(TASK_ID, TaskStatus.TODO, EMAIL_TESTE))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("🗑️ Método: deletarTask")
    class DeletarTaskTests {

        @Test
        @DisplayName("✅ Deve expurgar/deletar task com sucesso")
        void deveDeletarTaskComSucesso() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.findByIdAndUsuarioId(TASK_ID, USUARIO_ID)).thenReturn(Optional.of(taskTeste));

            taskService.deletarTask(TASK_ID, EMAIL_TESTE);

            verify(taskRepository).delete(taskTeste);
        }
    }

    @Nested
    @DisplayName("📊 Método: getEstatisticas")
    class GetEstatisticasTests {

        @Test
        @DisplayName("✅ Deve calcular os agrupamentos quantitativos por status")
        void deveGerarEstatisticasComSucesso() {
            when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuarioTeste));
            when(taskRepository.countByUsuarioAndStatus(usuarioTeste, TaskStatus.TODO)).thenReturn(5L);
            when(taskRepository.countByUsuarioAndStatus(usuarioTeste, TaskStatus.DOING)).thenReturn(3L);
            when(taskRepository.countByUsuarioAndStatus(usuarioTeste, TaskStatus.DONE)).thenReturn(10L);
            when(taskRepository.countByUsuarioId(USUARIO_ID)).thenReturn(18L);

            var stats = taskService.getEstatisticas(EMAIL_TESTE);

            assertThat(stats).isNotNull();
            assertThat(stats.get("totalGeral")).isEqualTo(18L);
        }
    }

    @Nested
    @DisplayName("🔄 Mapeamentos de Infraestrutura (DTO / Entities)")
    class ConverterParaDTOTests {

        @Test
        @DisplayName("✅ Deve acionar o MapStruct para conversões unitárias")
        void deveConverterTaskParaTaskDTO() {
            when(taskMapper.toDTO(taskTeste)).thenReturn(taskDTOTeste);
            TaskDTO resultado = taskService.converterParaDTO(taskTeste);
            assertThat(resultado).isNotNull();
        }
    }
}