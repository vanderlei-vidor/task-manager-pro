// ========================================
// SIDEBAR - MENU MOBILE COM OVERLAY
// ========================================
import { $, $$ } from '../core/utils.js';

class SidebarManager {
  constructor() {
    this.btnMenu = $('#btnMenuMobile');
    this.sidebar = $('.sidebar');
    this.overlay = $('#sidebarOverlay'); // ✅ NOVO
  }

  init() {
    if (!this.sidebar) return;
    
    console.log('📱 Sidebar Manager inicializado');
    this._bindEvents();
    this._updateActiveItem();
  }

  toggle() {
    if (!this.sidebar) return;
    
    this.sidebar.classList.toggle('active');
    
    // ✅ Toggle overlay junto
    if (this.overlay) {
      this.overlay.classList.toggle('active');
    }
    
    // Bloqueia scroll do body quando sidebar aberta
    document.body.style.overflow = this.sidebar.classList.contains('active') ? 'hidden' : '';
  }

  close() {
    if (this.sidebar) {
      this.sidebar.classList.remove('active');
    }
    if (this.overlay) {
      this.overlay.classList.remove('active');
    }
    document.body.style.overflow = '';
  }

  _bindEvents() {
    // 🍔 Botão hamburger
    if (this.btnMenu) {
      this.btnMenu.addEventListener('click', (e) => {
        e.stopPropagation();
        this.toggle();
      });
    }

    // 🎯 Click no overlay fecha sidebar
    if (this.overlay) {
      this.overlay.addEventListener('click', () => this.close());
    }

    // ✅ Fecha ao clicar fora (em mobile)
    document.addEventListener('click', (e) => {
      if (window.innerWidth <= 992) {
        if (this.sidebar &&
            !this.sidebar.contains(e.target) &&
            this.btnMenu &&
            !this.btnMenu.contains(e.target)) {
          this.close();
        }
      }
    });

    // ✅ Fecha ao redimensionar para desktop
    window.addEventListener('resize', () => {
      if (window.innerWidth > 992) {
        this.close();
      }
    });

    // ✅ Fecha ao clicar em um link da sidebar (mobile)
    const navLinks = $$('.sidebar-nav .nav-item');
    navLinks.forEach(link => {
      link.addEventListener('click', () => {
        if (window.innerWidth <= 992) {
          this.close();
        }
      });
    });

    // ✅ Fecha com tecla ESC
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.sidebar?.classList.contains('active')) {
        this.close();
      }
    });
  }

  _updateActiveItem() {
    const currentPage = $('[data-current-page]');
    if (!currentPage) return;

    const page = currentPage.getAttribute('data-current-page');
    const navItem = $(`.nav-item[href="/${page}"]`);

    if (navItem) {
      $$('.nav-item').forEach(item => item.classList.remove('active'));
      navItem.classList.add('active');
    }
  }
}

export const Sidebar = new SidebarManager();