// ========================================
// GESTOR PRO - ENTRY POINT
// ========================================

// Core
import { Toast } from './core/toast.js';
import { $, $$ } from './core/utils.js'; // 🚀 Usando exclusivamente os utilitários do core

// Components
import { Theme } from './components/theme.js';
import { Sidebar } from './components/sidebar.js';

// Features
import { Kanban } from './features/kanban.js';
import { Calendar } from './features/calendar.js';
import { Shortcuts } from './features/shortcuts.js';

// Pages
import { Reports } from './pages/reports.js';

// ========================================
// INICIALIZAÇÃO
// ========================================
document.addEventListener('DOMContentLoaded', () => {
  // 1. Inicializa componentes base (Globais)
  Toast.init();
  Theme.init();
  Sidebar.init();
  Shortcuts.init();

  // 2. Inicializa features específicas 
  // 💡 Como adicionamos as linhas de defesa dentro de cada uma delas, 
  // elas só vão rodar se os elementos correspondentes existirem na página atual!
  Kanban.init();
  Calendar.init();
  Reports.init(); // 🚀 Chamado de forma limpa e autoprotegida

  // 3. Converte flash messages do Spring Boot em toasts
  _convertFlashMessages();

  // 4. Animação de entrada dos cards
  _animateCards();

  // 5. Toast de boas-vindas controlado (Apenas uma vez por sessão)
  _showWelcomeMessage();

  console.log('🚀 Gestor Pro carregado com sucesso!');
});

// ========================================
// FUNÇÕES AUXILIARES
// ========================================

function _convertFlashMessages() {
  const alertSuccess = $('.alert-success');
  const alertDanger = $('.alert-danger');

  if (alertSuccess) {
    Toast.success('Sucesso!', alertSuccess.textContent.trim());
    alertSuccess.remove();
  }

  if (alertDanger) {
    Toast.error('Erro!', alertDanger.textContent.trim());
    alertDanger.remove();
  }
}

function _animateCards() {
  const cards = $$('.stat-card, .card-premium');
  cards.forEach((card, index) => {
    card.style.opacity = '0';
    card.style.transform = 'translateY(20px)';

    setTimeout(() => {
      card.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
      card.style.opacity = '1';
      card.style.transform = 'translateY(0)';
    }, index * 50);
  });
}

function _showWelcomeMessage() {
  // 💡 Blinda para exibir o toast apenas na primeira página que o usuário abrir na sessão
  if (!sessionStorage.getItem('welcomeShown')) {
    setTimeout(() => {
      const nomeUsuario = $('.user-name')?.textContent || 'Usuário';
      Toast.success(`Bem-vindo, ${nomeUsuario}! 👋`, 'Pressione ? para ver os atalhos de teclado');
      sessionStorage.setItem('welcomeShown', 'true'); // Marca que já foi exibido
    }, 600);
  }
}

// 💡 Removidas as funções duplicadas $(selector) e $$(selector) que estavam no final do arquivo!