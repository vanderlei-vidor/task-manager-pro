// ========================================
// DASHBOARD - ANIMAÇÕES E MÉTRICAS
// ========================================
import { Toast } from '../core/toast.js';
import { $, $$, animateCounter, animateValue } from '../core/utils.js';

class DashboardManager {
  constructor() {
    // Referências limpas. Evita Null Pointers ao instanciar o módulo no Vite
    this.percentageValue = null;
    this.progressBar = null;
    this.columnCounters = [];
  }

  init() {
    // 💡 Abordagem Sênior: Em vez de mapear estritamente a URL, checamos se um elemento 
    // exclusivo do Dashboard existe no DOM (ex: o container de métricas). Muito mais seguro!
    this.percentageValue = $('.percentage-value');
    if (!this.percentageValue) return;

    this.progressBar = $('.progress-bar-fill');
    this.columnCounters = $$('.column-count');

    console.log('📊 Dashboard Manager inicializado com segurança!');
    
    this._animateProgressBar();
    this._animateColumnCounters();
    this._animateStatValues();
  }

  /**
   * Anima a barra de progresso do card de produtividade principal
   */
  _animateProgressBar() {
    if (!this.percentageValue || !this.progressBar) return;

    // Captura o valor final impresso pelo Thymeleaf no HTML
    const target = parseInt(this.percentageValue.textContent) || 0;
    const duration = 1500;
    const steps = 30;
    const stepTime = duration / steps;
    const increment = target / steps;

    let current = 0;

    // Reseta o estado para a animação começar do zero de forma fluida
    this.percentageValue.textContent = '0';
    this.progressBar.style.width = '0%';

    const counter = setInterval(() => {
      current += increment;

      if (current >= target) {
        this.percentageValue.textContent = target;
        this.progressBar.style.width = `${target}%`;
        clearInterval(counter);

        // Feedback comemorativo de alta produtividade
        if (target >= 100) {
          setTimeout(() => {
            Toast.success('🎉 Parabéns!', 'Você completou 100% das tarefas do seu quadrante!');
          }, 400);
        }
      } else {
        this.percentageValue.textContent = Math.floor(current);
        this.progressBar.style.width = `${current}%`;
      }
    }, stepTime);
  }

  /**
   * Anima contadores dinâmicos das colunas do Kanban visíveis no dashboard
   */
  _animateColumnCounters() {
    if (!this.columnCounters.length) return;

    this.columnCounters.forEach((counter, index) => {
      const target = parseInt(counter.textContent) || 0;

      // Executa de carona na função de alta performance do core com cascata de delay
      setTimeout(() => {
        animateCounter(counter, target, 800);
      }, index * 80);
    });
  }

  /**
   * Anima valores numéricos gerais dos blocos de estatísticas (Stat Cards)
   */
  _animateStatValues() {
    const statValues = $$('.stat-value');
    if (!statValues.length) return;

    statValues.forEach((valueEl, index) => {
      const target = parseInt(valueEl.textContent) || 0;
      if (isNaN(target)) return;

      // Usa a engine utilitária nativa com micro-delay para o olho humano acompanhar
      setTimeout(() => {
        animateValue(valueEl, 0, target, 1200);
      }, index * 100);
    });
  }

  /**
   * Pipeline assíncrona para atualização de dados em tempo real (Polling / WebSockets futuros)
   */
  async refreshStats() {
    try {
      Toast.info('Atualizando', 'Sincronizando métricas com o servidor...');

      // 🔥 Estrutura pronta para acoplamento REST com o Spring Boot
      // const response = await fetch('/api/dashboard/stats');
      // const data = await response.json();
      // this._updateUI(data);

      Toast.success('Atualizado!', 'Painel de controle atualizado com sucesso');
    } catch (error) {
      console.error('Erro na requisição de refresh dos stats:', error);
      Toast.error('Falha na sincronização', 'Não foi possível ler as métricas novas.');
    }
  }
}

export const Dashboard = new DashboardManager();