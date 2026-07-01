// ========================================
// SIDEBAR - MENU MOBILE
// ========================================
import { $, $$ } from '../core/utils.js'; // 🚀 Importado o $$ do seu core
import { Config } from '../core/config.js'; // 🚀 Importado para usar os breakpoints centrais

class SidebarManager {
  constructor() {
    this.btnMenu = null;
    this.sidebar = null;
  }

  init() {
    // 💡 Captura os elementos de forma segura com o DOM já renderizado
    this.btnMenu = $('#btnMenuMobile');
    this.sidebar = $('.sidebar');

    this._bindEvents();
    this._updateActiveItem();
  }

  toggle() {
    if (this.sidebar) {
      this.sidebar.classList.toggle('active');
    }
  }

  close() {
    if (this.sidebar) {
      this.sidebar.classList.remove('active');
    }
  }

  _bindEvents() {
    if (this.btnMenu) {
      this.btnMenu.addEventListener('click', () => this.toggle());
    }

    // Fecha sidebar ao clicar fora (mobile)
    document.addEventListener('click', (e) => {
      // 💡 Usando o breakpoint centralizado do seu config.js
      if (window.innerWidth <= Config.breakpoints.desktop) {
        if (this.sidebar &&
            !this.sidebar.contains(e.target) &&
            this.btnMenu &&
            !this.btnMenu.contains(e.target)) {
          this.close();
        }
      }
    });
  }

  _updateActiveItem() {
    const currentPage = $('[data-current-page]');
    if (!currentPage) return;

    const page = currentPage.getAttribute('data-current-page');
    const navItem = $(`.nav-item[href="/${page}"]`);

    if (navItem) {
      // 💡 Agora usando com segurança o $$ do utils.js, limpando o escopo do arquivo
      $$('.nav-item').forEach(item => item.classList.remove('active'));
      navItem.classList.add('active');
    }
  }
}

export const Sidebar = new SidebarManager();