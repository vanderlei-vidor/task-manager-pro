// ========================================
// ATALHOS DE TECLADO - GESTOR PRO
// ========================================

class ShortcutsManager {
  constructor() {
    this.shortcuts = [
      { keys: ['g', 'd'], label: 'Ir para Dashboard' },
      { keys: ['g', 'k'], label: 'Ir para Kanban' },
      { keys: ['g', 'c'], label: 'Ir para Calendário' },
      { keys: ['g', 't'], label: 'Ir para Tags' },
      { keys: ['g', 'r'], label: 'Ir para Relatórios' },
      { keys: ['Ctrl', 'N'], label: 'Nova tarefa' },
      { keys: ['Ctrl', 'K'], label: 'Buscar tarefas' },
      { keys: ['Ctrl', 'D'], label: 'Alternar tema' },
      { keys: ['?'], label: 'Mostrar atalhos' },
      { keys: ['Esc'], label: 'Fechar modal' },
      { keys: ['Ctrl', 'Shift', 'L'], label: 'Fazer logout' }
    ];
  }

  init() {
    console.log('🎹 Shortcuts Manager inicializado');
    this._bindEvents();
  }

  _bindEvents() {
    // Tecla "?" abre o modal
    document.addEventListener('keydown', (e) => {
      const isTyping = ['INPUT', 'TEXTAREA', 'SELECT'].includes(e.target.tagName);
      if (isTyping) return;

      if (e.key === '?' || (e.shiftKey && e.key === '/')) {
        e.preventDefault();
        this.showHelp();
      }
    });
  }

  // ✅ Abre o modal Bootstrap
  showHelp() {
    const modalElement = document.querySelector('#modalAtalhos');
    if (modalElement) {
      const modal = new bootstrap.Modal(modalElement);
      modal.show();
      console.log('🎹 Modal de atalhos aberto');
    } else {
      console.error('❌ Modal #modalAtalhos não encontrado!');
    }
  }
}

export const Shortcuts = new ShortcutsManager();
