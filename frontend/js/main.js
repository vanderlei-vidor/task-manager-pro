// ========================================
// GESTOR PRO - ENTRY POINT (MAESTRO)
// ========================================

// Core
import { Toast } from './core/toast.js';
import { $, $$ } from './core/utils.js';

// Components
import { Theme } from './components/theme.js';
import { Sidebar } from './components/sidebar.js';
import { Search } from './components/search.js';


// Features
import { Kanban } from './features/kanban.js';
import { Calendar } from './features/calendar.js';
import { Shortcuts } from './features/shortcuts.js';
import { Tags } from './features/tags.js';

// Pages / Features de Rota
import { Reports } from './pages/reports.js'; 
import { Dashboard } from './pages/dashboard.js';

// ========================================
// INICIALIZAÇÃO DO ECOSSISTEMA
// ========================================
document.addEventListener('DOMContentLoaded', () => {
  // 1. Inicializa componentes de layout base (Sempre ativos)
  Toast.init();
  Theme.init();
  Sidebar.init();
  
  Search.init();

  // 2. Inicializa features globais baseadas em presença de elementos
  Kanban.init();
  Calendar.init();
  Tags.init();
  Shortcuts.init();

  // 3. Carrega páginas específicas baseado no roteamento da URL
  _loadPageScripts();

  // 4. Captura interceptadores do backend (Flash Messages)
  _convertFlashMessages();

  // 5. Aplica patches de correção de Z-Index e foco nos Modais do Bootstrap
  _fixModalInputs();

  // 6. Atualiza o estado visual da Sidebar
  _updateActiveNavItem();

  // 7. 🔥 Toast de boas-vindas controlado por sessão (Aparece apenas uma vez)
  _showWelcomeToast();

  console.log('🚀 Gestor Pro inicializado e monitorado via Vite!');
});

// ========================================
// FUNÇÕES AUXILIARES DE SUPORTE
// ========================================

/**
 * Roteador lógico para acoplamento de engines de páginas específicas
 */
function _loadPageScripts() {
  const path = window.location.pathname;

  if (path === '/relatorios') {
    Reports.init();
  } else if (path === '/' || path === '/dashboard') {
    Dashboard.init();
  }
}

/**
 * Converte flash messages do Spring Boot / Thymeleaf em Toasts elegantes
 */
function _convertFlashMessages() {
  const alertSuccess = $('.alert-success');
  const alertDanger = $('.alert-danger');
  const alertWarning = $('.alert-warning');

  if (alertSuccess) {
    Toast.success('Sucesso!', alertSuccess.textContent.trim());
    alertSuccess.remove();
  }

  if (alertDanger) {
    Toast.error('Erro!', alertDanger.textContent.trim());
    alertDanger.remove();
  }

  if (alertWarning) {
    Toast.warning('Atenção!', alertWarning.textContent.trim());
    alertWarning.remove();
  }
}

/**
 * Corrige falhas nativas de foco e clicks de elementos sobrepostos em modais
 */
function _fixModalInputs() {
  document.addEventListener('shown.bs.modal', (event) => {
    const modal = event.target;

    // Força a re-habilitação dos ponteiros de eventos nos inputs do formulário
    const inputs = modal.querySelectorAll('input, textarea, select');
    inputs.forEach(input => {
      input.style.pointerEvents = 'auto';
      input.style.position = 'relative';
      input.style.zIndex = '10';
    });

    // Garante que as labels customizadas de tags respondam perfeitamente ao clique
    const tagLabels = modal.querySelectorAll('.tag-label');
    tagLabels.forEach(label => {
      label.style.pointerEvents = 'auto';
      label.style.cursor = 'pointer';
    });

    console.log('🎯 Ciclo do Bootstrap Modal: Inputs re-alinhados.');
  });
}

/**
 * Sincroniza a classe de ativação do item da Sidebar baseado no data-attribute do HTML
 */
function _updateActiveNavItem() {
  $$('.nav-item').forEach(item => item.classList.remove('active'));

  const currentPage = $('[data-current-page]');
  if (!currentPage) return;

  const page = currentPage.getAttribute('data-current-page');
  const navItem = $(`.nav-item[href="/${page}"]`);

  if (navItem) {
    navItem.classList.add('active');
  }
}

/**
 * Gerencia o disparo do Toast de boas-vindas usando Session Storage para evitar loops
 */
function _showWelcomeToast() {
  if (sessionStorage.getItem('welcomeShown')) return;

  setTimeout(() => {
    const nomeUsuario = $('.user-name')?.textContent.trim() || 'Usuário';
    Toast.success(`Bem-vindo, ${nomeUsuario}! 👋`, 'Pressione ? para ver os atalhos de teclado.');
    sessionStorage.setItem('welcomeShown', 'true');
  }, 600);
}