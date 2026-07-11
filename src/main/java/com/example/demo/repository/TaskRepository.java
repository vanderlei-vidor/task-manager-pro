package com.example.demo.repository;

import com.example.demo.model.Task;
import com.example.demo.model.TaskStatus;
import com.example.demo.model.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ========================================
    // MÉTODOS AUTOMÁTICOS
    // ========================================

    List<Task> findByUsuario(Usuario usuario);

    List<Task> findByUsuarioId(Long usuarioId);

    // ✅ NOVO: Busca tasks com tags já carregadas (evita
    // LazyInitializationException!)
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.usuario.id = :usuarioId")
    List<Task> findByUsuarioIdWithTags(@Param("usuarioId") Long usuarioId);

    // ✅ PAGINAÇÃO: Mesma query de cima, mas paginada.
    // countQuery explícito é OBRIGATÓRIO porque DISTINCT + JOIN FETCH
    // quebra a derivação automática do count do Spring Data.
    @Query(
        value = "SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.usuario.id = :usuarioId",
        countQuery = "SELECT COUNT(DISTINCT t) FROM Task t WHERE t.usuario.id = :usuarioId"
    )
    Page<Task> findByUsuarioIdWithTagsPaginado(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // ✅ NOVO: Busca por status com tags carregadas
    @EntityGraph(attributePaths = { "tags" })
    List<Task> findByUsuarioAndStatus(Usuario usuario, TaskStatus status);

    /**
     * ✅ NOVO! Conta TODAS as tasks de um usuário
     */
    long countByUsuarioId(Long usuarioId);

    long countByUsuarioAndStatus(Usuario usuario, TaskStatus status);

    List<Task> findByUsuarioAndTitleContainingIgnoreCase(Usuario usuario, String title);

    List<Task> findByUsuarioAndStatusNotAndDueDateBefore(
            Usuario usuario,
            TaskStatus status,
            LocalDate date);

    // ========================================
    // QUERIES CUSTOMIZADAS (JPQL)
    // ========================================

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.usuario = :usuario GROUP BY t.status")
    List<Object[]> getEstatisticasPorStatus(@Param("usuario") Usuario usuario);

    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.usuario.id = :usuarioId")
    Optional<Task> findByIdAndUsuarioId(
            @Param("taskId") Long taskId,
            @Param("usuarioId") Long usuarioId);

    @Query("SELECT t FROM Task t WHERE t.usuario = :usuario " +
            "AND t.status <> com.example.demo.model.TaskStatus.DONE " +
            "AND t.dueDate BETWEEN :hoje AND :dataLimite " +
            "ORDER BY t.dueDate ASC")
    List<Task> findTasksProximasDoVencimento(
            @Param("usuario") Usuario usuario,
            @Param("hoje") LocalDate hoje,
            @Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT DISTINCT t FROM Task t WHERE t.usuario = :usuario " +
            "AND t.status = :status ORDER BY t.dataCriacao DESC")
    List<Task> findByUsuarioAndStatusOrderByDataCriacaoDesc(
            @Param("usuario") Usuario usuario,
            @Param("status") TaskStatus status);
}