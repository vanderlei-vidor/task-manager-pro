// ========================================
// KANBAN - FEATURE COMPLETA (Versão Blindada)
// Filtros + Drag & Drop + Contadores Animados
// ========================================
import { Toast } from '../core/toast.js';
import { apiFetchWithRefresh } from '../core/api.js';
import { $, $$, animateCounter } from '../core/utils.js';

class KanbanManager {
  constructor() {
    this.filterButtons = [];
    this.taskCards = [];
    this.columns = [];
    this.draggedCard = null;
    this.originalColumn = null;

    this.statusLabels = {
      'TODO': '📋 A Fazer',
      'DOING': '⚡ Em Andamento',
      'DONE': '✅ Concluída'
    };
  }

  init() {
    this.filterButtons = $$('.filter-btn');
    this.taskCards = $$('.task-card');
    this.columns = $$('.kanban-column');

    if (this.columns.length === 0) return;

    console.log('🎯 Kanban Manager inicializado');

    this._initFilters();
    this._initDragAndDrop();
    this._animateColumnCounters();
  }

  // ========================================
  // 🎨 FILTROS
  // ========================================
  _initFilters() {
    if (this.filterButtons.length === 0) return;

    this.filterButtons.forEach(btn => {
      btn.addEventListener('click', () => this._handleFilter(btn));
    });
  }

  _handleFilter(activeBtn) {
    this.filterButtons.forEach(b => b.classList.remove('active'));
    activeBtn.classList.add('active');

    const filter = activeBtn.dataset.filter;

    this.taskCards.forEach(card => {
      const priority = card.dataset.priority;
      const isOverdue = card.classList.contains('card-overdue');

      let show = false;
      if (filter === 'all') show = true;
      else if (filter === 'overdue') show = isOverdue;
      else show = priority === filter;

      if (show) {
        card.style.display = 'flex';
        card.style.animation = 'slideInUp 0.3s ease-out';
      } else {
        card.style.display = 'none';
      }
    });
  }

  // ========================================
  // 🎯 DRAG AND DROP
  // ========================================
  _initDragAndDrop() {
    this._bindDragEvents();
    this._bindDropEvents();
  }

  _bindDragEvents() {
    this.taskCards.forEach(card => {
      card.setAttribute('draggable', 'true');

      card.addEventListener('dragstart', (e) => {
        this.draggedCard = card;
        this.originalColumn = card.closest('.kanban-column');

        setTimeout(() => card.classList.add('dragging'), 0);

        e.dataTransfer.effectAllowed = 'move';

        const taskId = card.dataset.taskId || card.dataset.id;
        e.dataTransfer.setData('text/plain', taskId);

        console.log(`🎯 Drag iniciado: Task ${taskId}`);
      });

      card.addEventListener('dragend', () => {
        card.classList.remove('dragging');
        this.columns.forEach(col => {
          col.querySelector('.column-body')?.classList.remove('drag-over');
        });
        // 💡 Limpa o estado global de forma segura aqui
        this.draggedCard = null;
        this.originalColumn = null;
      });
    });
  }

  _bindDropEvents() {
    this.columns.forEach(column => {
      const columnBody = column.querySelector('.column-body');
      if (!columnBody) return;

      columnBody.addEventListener('dragover', (e) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        columnBody.classList.add('drag-over');
      });

      columnBody.addEventListener('dragleave', (e) => {
        if (!columnBody.contains(e.relatedTarget)) {
          columnBody.classList.remove('drag-over');
        }
      });

      columnBody.addEventListener('drop', (e) => {
        e.preventDefault();
        columnBody.classList.remove('drag-over');

        if (!this.draggedCard) return;

        const taskId = e.dataTransfer.getData('text/plain');

        const newStatus = this._getColumnStatus(column);
        const oldStatus = this._getColumnStatus(this.originalColumn);

        // 💡 CAPTURA SEGURA: Guarda o card e a coluna atual em constantes antes que fiquem null
        const targetCard = this.draggedCard;
        const sourceColumn = this.originalColumn;

        console.log('🎯 DROP!', { taskId, newStatus, oldStatus, column: column.className });

        if (!newStatus || !oldStatus) {
          console.error('❌ Status não encontrado nas colunas!');
          return;
        }

        if (newStatus === oldStatus) {
          console.log('🔄 Mesma coluna, ignorando');
          return;
        }

        // Move o card visualmente usando a constante local
        columnBody.appendChild(targetCard);
        targetCard.classList.remove('dragging');

        targetCard.style.animation = 'none';
        setTimeout(() => {
          targetCard.style.animation = 'slideInUp 0.3s ease-out';
        }, 10);

        this._updateColumnCounters();

        // 💡 Passa as referências fixas para evitar problemas de concorrência na API
        this._updateTaskStatus(taskId, newStatus, oldStatus, targetCard, sourceColumn);

        console.log(`✅ Task ${taskId} movida: ${oldStatus} → ${newStatus}`);
      });
    });
  }

  _getColumnStatus(column) {
    if (!column) return null;
    if (column.dataset.status) return column.dataset.status.toUpperCase();
    if (column.classList.contains('column-todo')) return 'TODO';
    if (column.classList.contains('column-doing')) return 'DOING';
    if (column.classList.contains('column-done')) return 'DONE';

    const title = column.querySelector('.column-title')?.textContent.toLowerCase() || '';
    if (title.includes('fazer') || title.includes('todo')) return 'TODO';
    if (title.includes('andamento') || title.includes('doing')) return 'DOING';
    if (title.includes('conclu') || title.includes('done')) return 'DONE';

    return null;
  }

  // ========================================
  // 🔄 ATUALIZAR STATUS NO BACKEND
  // ========================================
  async _updateTaskStatus(taskId, newStatus, oldStatus, card, sourceColumn) {
    if (!taskId) {
      Toast.error('Erro', 'ID da tarefa não encontrado');
      return;
    }

    try {
      const response = await apiFetchWithRefresh(`/api/tasks/${taskId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
      });

      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      Toast.success('Tarefa movida!', `Movida para ${this.statusLabels[newStatus]}`);

      // 💡 Atualiza a badge do card correto recebido por parâmetro
      this._updateCardBadge(card, newStatus);

      if (newStatus === 'DONE') {
        Toast.success('🎉 Parabéns!', 'Tarefa concluída com sucesso!');
      }

    } catch (error) {
      console.error('❌ Erro ao atualizar status:', error);

      // 💡 Rollback ultra seguro usando referências da constante local
      if (sourceColumn && card) {
        const originalBody = sourceColumn.querySelector('.column-body');
        originalBody?.appendChild(card);
        this._updateColumnCounters();
      }

      Toast.error('Erro ao mover tarefa', 'A tarefa voltou para a coluna original');
    }
  }

  // ========================================
  // 🔢 CONTADORES
  // ========================================
  _animateColumnCounters() {
    const counters = $$('.column-count');
    counters.forEach(counter => {
      const target = parseInt(counter.textContent) || 0;
      animateCounter(counter, target, 800);
    });
  }

  _updateColumnCounters() {
    this.columns.forEach(column => {
      const body = column.querySelector('.column-body');
      const counter = column.querySelector('.column-count');

      if (body && counter) {
        const count = body.querySelectorAll('.task-card').length;
        counter.textContent = count;

        counter.style.animation = 'scaleIn 0.3s ease-out';
        setTimeout(() => { counter.style.animation = ''; }, 300);
      }
    });
  }

  // ========================================
  // 🏷️ BADGE DO CARD
  // ========================================
  _updateCardBadge(card, newStatus) {
    const badge = card.querySelector('.badge-status');
    if (!badge) return;

    const statusConfig = {
      'TODO': { class: 'badge-warning', text: 'A Fazer' },
      'DOING': { class: 'badge-info', text: 'Em Andamento' },
      'DONE': { class: 'badge-success', text: 'Concluída' }
    };

    const config = statusConfig[newStatus];
    if (!config) return;

    badge.classList.remove('badge-warning', 'badge-info', 'badge-success');
    badge.classList.add(config.class);
    badge.textContent = config.text;
  }
}

export const Kanban = new KanbanManager();