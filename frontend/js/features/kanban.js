// ========================================
// KANBAN - FILTROS E INTERAÇÕES
// ========================================
import { $$, animateCounter } from '../core/utils.js';

class KanbanManager {
  constructor() {
    // 💡 Inicializa as propriedades vazias para o escopo da classe
    this.filterButtons = [];
    this.taskCards = [];
  }

  init() {
    // 🚀 Captura dinâmica: busca os elementos reais no momento em que o DOM está pronto
    this.filterButtons = $$('.filter-btn');
    this.taskCards = $$('.task-card');

    // Se não houver botões de filtro, significa que não estamos na página do Kanban. Corta o fluxo com segurança!
    if (this.filterButtons.length === 0) return;

    this._bindFilterEvents();
    this._animateColumnCounters();
  }

  _bindFilterEvents() {
    this.filterButtons.forEach(btn => {
      btn.addEventListener('click', () => this._handleFilter(btn));
    });
  }

  _handleFilter(activeBtn) {
    // Atualiza botão ativo
    this.filterButtons.forEach(b => b.classList.remove('active'));
    activeBtn.classList.add('active');

    const filter = activeBtn.dataset.filter;

    // Filtra cards
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

  _animateColumnCounters() {
    const counters = $$('.column-count');
    counters.forEach(counter => {
      // 💡 Pequena trava de segurança: garante que há um número antes de animar
      const target = parseInt(counter.textContent) || 0;
      animateCounter(counter, target, 800);
    });
  }
}

export const Kanban = new KanbanManager();