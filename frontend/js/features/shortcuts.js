// ========================================
// ATALHOS DE TECLADO GLOBAIS
// ========================================
import { Toast } from '../core/toast.js';
import { $, $$ } from '../core/utils.js'; // 🚀 Usando exclusivamente os utilitários do core
import { Theme } from '../components/theme.js';

class ShortcutsManager {
  constructor() {
    this.shortcuts = [
      { keys: ['Ctrl', 'N'], label: 'Nova tarefa' },
      { keys: ['Ctrl', 'K'], label: 'Busca rápida' },
      { keys: ['Ctrl', 'D'], label: 'Modo escuro' },
      { keys: ['Esc'], label: 'Fechar modal' },
      { keys: ['←', '→'], label: 'Navegar calendário' },
      { keys: ['?'], label: 'Esta ajuda' }
    ];
  }

  init() {
    document.addEventListener('keydown', (e) => this._handleKeydown(e));
    this._createHelpButton();
  }

  _handleKeydown(e) {
    // Ignora se o usuário estiver digitando em campos de texto
    if (e.target.tagName === 'INPUT' ||
        e.target.tagName === 'TEXTAREA' ||
        e.target.isContentEditable) {
      return;
    }

    // 🔥 Convertendo para lowercase para blindar contra Caps Lock ativo
    const keyLower = e.key ? e.key.toLowerCase() : '';

    // Ctrl + N = Nova tarefa
    if ((e.ctrlKey || e.metaKey) && keyLower === 'n') {
      e.preventDefault();
      this._openNewTaskModal();
    }

    // Ctrl + K = Foco na busca
    if ((e.ctrlKey || e.metaKey) && keyLower === 'k') {
      e.preventDefault();
      this._focusSearch();
    }

    // Ctrl + D = Toggle modo escuro
    if ((e.ctrlKey || e.metaKey) && keyLower === 'd') {
      e.preventDefault();
      Theme.toggle();
    }

    // Esc = Fecha modais
    if (e.key === 'Escape') {
      this._closeModals();
    }

    // ? = Mostra ajuda
    if (e.key === '?') {
      this.showHelp();
    }

    // Navegação do calendário
    if (window.location.pathname === '/calendario') {
      this._handleCalendarNavigation(e);
    }
  }

  _openNewTaskModal() {
    const modal = $('#modalNovaTarefa');
    if (modal) {
      // 🚀 Padrão Sênior: Recupera a instância existente ou cria uma se não houver (evita leak de memória)
      const bsModal = bootstrap.Modal.getOrCreateInstance(modal);
      bsModal.show();
      Toast.info('Atalho', 'Modal de nova tarefa aberto');
    }
  }

  _focusSearch() {
    const search = $('#globalSearch');
    if (search) {
      search.focus();
      Toast.info('Busca', 'Foco na busca global');
    }
  }

  _closeModals() {
    $$('.modal.show').forEach(modal => {
      const bsModal = bootstrap.Modal.getInstance(modal);
      if (bsModal) bsModal.hide();
    });
  }

  _handleCalendarNavigation(e) {
    const btnPrev = $('#btnMesAnterior');
    const btnNext = $('#btnMesProximo');

    if (e.key === 'ArrowLeft' && btnPrev) btnPrev.click();
    else if (e.key === 'ArrowRight' && btnNext) btnNext.click();
  }

  showHelp() {
    // 💡 Montagem do HTML limpa consumindo as chaves do construtor
    const message = this.shortcuts.map(s => `
      <div style="display:flex;align-items:center;gap:8px;margin:4px 0;">
        ${s.keys.map(k => `<span class="shortcut-key">${k}</span>`).join('')}
        <span style="color:var(--text-secondary);font-size:13px;">${s.label}</span>
      </div>
    `).join('');

    Toast.info('Atalhos de Teclado', message, 8000);
  }

  _createHelpButton() {
    const btn = document.createElement('button');
    btn.className = 'btn-help';
    btn.innerHTML = '<i class="bi bi-question-lg"></i>';
    btn.title = 'Atalhos de teclado (?)';
    btn.addEventListener('click', () => this.showHelp()); // 🚀 Removido onclick inline
    document.body.appendChild(btn);
  }
}

// 💡 Removidas as funções duplicadas $(selector) e $$(selector) que estavam no final do arquivo!

export const Shortcuts = new ShortcutsManager();