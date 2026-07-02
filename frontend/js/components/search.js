// ========================================
// BUSCA GLOBAL - FILTRO DE TABELAS
// ========================================
import { Toast } from '../core/toast.js';
import { $, $$ } from '../core/utils.js';

class SearchManager {
  constructor() {
    // Estado limpo. DOM só é mapeado na inicialização real da página.
    this.searchInput = null;
    this.tableRows = [];
    this.noResultsMessage = null;
  }

  init() {
    this.searchInput = $('#globalSearch');
    
    // Linha de defesa: se não houver barra de busca nessa tela, mata a execução
    if (!this.searchInput) return;

    // 🔥 Captura apenas linhas reais, ignorando a linha de aviso se ela já existir
    this.tableRows = $$('tbody tr:not(.no-results-row)');

    console.log('🔍 Search Manager inicializado com segurança!');
    
    this._createNoResultsMessage();
    this._bindEvents();
  }

  /**
   * Configura eventos de entrada de dados com debounce de performance
   */
  _bindEvents() {
    // Importa dinamicamente ou assume o utilitário do core para não travar a UI
    const debouncedSearch = this._debounce((query) => this._performSearch(query), 200);

    this.searchInput.addEventListener('input', (e) => {
      const query = e.target.value.toLowerCase().trim();
      debouncedSearch(query);
    });

    // Atalhos locais focados no comportamento do próprio Input
    this.searchInput.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        this.clear();
        this.searchInput.blur();
      }
    });
  }

  /**
   * Executa a busca nos dados textuais da tabela
   */
  _performSearch(query) {
    if (!this.tableRows.length) return;

    let visibleCount = 0;

    this.tableRows.forEach((row, index) => {
      // Captura o texto das colunas principais de forma segura
      const title = row.querySelector('td:first-child')?.textContent.toLowerCase() || '';
      const description = row.querySelector('.task-description')?.textContent.toLowerCase() || '';
      const tags = row.querySelector('.task-tags')?.textContent.toLowerCase() || '';

      // Varredura lógica multi-campo
      const matches = !query ||
        title.includes(query) ||
        description.includes(query) ||
        tags.includes(query);

      if (matches) {
        row.style.display = '';
        // Efeito cascata sutil usando CSS animations nativas
        row.style.animation = `fadeIn 0.2s ease-out ${index * 0.01}s backwards`;
        visibleCount++;
      } else {
        row.style.display = 'none';
      }
    });

    // Controla o estado da linha de "Nenhum resultado"
    this._toggleNoResultsMessage(visibleCount === 0 && query.length > 0);

    // Feedback visual de aviso no input se nada for encontrado
    if (query.length > 0 && visibleCount === 0) {
      this._showNoResultsFeedback();
    }
  }

  /**
   * Cria e injeta estruturalmente a linha de "sem resultados" no corpo da tabela
   */
  _createNoResultsMessage() {
    const table = $('table.table-premium');
    if (!table) return;

    const tbody = table.querySelector('tbody');
    if (!tbody) return;

    // Evita duplicar a linha caso o init() rode duas vezes na mesma view
    if ($('.no-results-row')) {
      this.noResultsMessage = $('.no-results-row');
      return;
    }

    this.noResultsMessage = document.createElement('tr');
    this.noResultsMessage.className = 'no-results-row';
    this.noResultsMessage.innerHTML = `
      <td colspan="100%" style="text-align: center; padding: 40px 20px; color: var(--text-tertiary);">
        <i class="bi bi-search" style="font-size: 32px; opacity: 0.5; display: block; margin-bottom: 12px;"></i>
        <p style="margin: 0; font-size: 14px; font-weight: 500;">Nenhum resultado encontrado</p>
        <p style="margin: 4px 0 0 0; font-size: 12px; opacity: 0.8;">Tente buscar por outro termo ou tag</p>
      </td>
    `;
    this.noResultsMessage.style.display = 'none';
    tbody.appendChild(this.noResultsMessage);
  }

  _toggleNoResultsMessage(show) {
    if (!this.noResultsMessage) return;
    this.noResultsMessage.style.display = show ? 'table-row' : 'none';
  }

  _showNoResultsFeedback() {
    if (!this.searchInput) return;
    this.searchInput.classList.add('search-empty-shake');
    this.searchInput.style.borderColor = 'var(--warning)';
    
    setTimeout(() => {
      this.searchInput.style.borderColor = '';
      this.searchInput.classList.remove('search-empty-shake');
    }, 500);
  }

  /**
   * Limpa o estado da busca e restaura todas as linhas originais
   */
  clear() {
    if (this.searchInput) {
      this.searchInput.value = '';
      this._performSearch('');
    }
  }

  getQuery() {
    return this.searchInput?.value || '';
  }

  /**
   * Fallback interno de Debounce caso não venha injetado do utils
   */
  _debounce(fn, delay) {
    let timeout;
    return (...args) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => fn(...args), delay);
    };
  }
}

export const Search = new SearchManager();