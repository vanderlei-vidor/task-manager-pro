// ========================================
// RELATÓRIOS - GRÁFICOS E HEATMAP
// ========================================
import { Config } from '../core/config.js';
import { animateValue } from '../core/utils.js';

class ReportsManager {
  constructor() {
    this.colors = Config.colors;
  }

  init() {
    // 🚀 Linha de defesa: Se não houver o container do heatmap, significa que o usuário não está na página de relatórios. Corta a execução!
    if (!document.getElementById('heatmap')) return;

    this._initProgressRing();
    this._initStatusChart();
    this._initPriorityChart();
    this._initDayOfWeekChart();
    this._generateHeatmap();

    console.log('📊 Relatórios carregados com sucesso!');
  }

  _initProgressRing() {
    const progressRing = document.querySelector('.progress-ring-fill');
    if (!progressRing) return;

    const percent = parseFloat(progressRing.dataset.percent) || 0;
    const circumference = 2 * Math.PI * 85;
    const offset = circumference - (percent / 100) * circumference;

    setTimeout(() => {
      progressRing.style.strokeDashoffset = offset;
    }, 300);

    const valueEl = document.querySelector('.progress-ring-value');
    if (valueEl) {
      animateValue(valueEl, 0, percent, 1500);
    }
  }

  _initStatusChart() {
    const el = document.getElementById('chartStatus');
    if (!el) return;

    const ctx = el.getContext('2d');
    
    // 💡 Captura os dados do dataset convertendo de forma segura para inteiro
    const todoCount = parseInt(el.dataset.statusTodo) || 0;
    const doingCount = parseInt(el.dataset.statusDoing) || 0;
    const doneCount = parseInt(el.dataset.statusDone) || 0;

    new Chart(ctx, {
      type: 'doughnut',
      data: {
        // 🔥 Ordem corrigida e alinhada: Dados -> Cores -> Rótulos
        labels: ['Pendentes', 'Em Andamento', 'Concluídas'],
        datasets: [{
          data: [todoCount, doingCount, doneCount],
          backgroundColor: [
            this.colors.warning, // Amarelo para Pendentes
            this.colors.info,    // Azul para Em Andamento
            this.colors.success  // Verde para Concluídas
          ],
          borderWidth: 0,
          hoverOffset: 8
        }]
      },
      options: this._getChartOptions()
    });
  }

  _initPriorityChart() {
    const el = document.getElementById('chartPriority');
    if (!el) return;

    const ctx = el.getContext('2d');
    
    // 💡 Preparado para quando você injetar dados dinâmicos do backend via dataset
    const alta = parseInt(el.dataset.priorityAlta) || 0;
    const media = parseInt(el.dataset.priorityMedia) || 0;
    const baixa = parseInt(el.dataset.priorityBaixa) || 0;

    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Alta', 'Média', 'Baixa'],
        datasets: [{
          label: 'Tarefas',
          data: el.dataset.priorityAlta ? [alta, media, baixa] : [3, 5, 2], // Fallback estático de teste
          backgroundColor: [
            this.colors.danger,
            this.colors.warning,
            this.colors.success
          ],
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: {
            beginAtZero: true,
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: { grid: { display: false } }
        }
      }
    });
  }

  _initDayOfWeekChart() {
    const el = document.getElementById('chartDayOfWeek');
    if (!el) return;

    const ctx = el.getContext('2d');
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'],
        datasets: [{
          label: 'Tarefas',
          data: [5, 8, 6, 9, 7, 3, 2],
          borderColor: this.colors.primary,
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
          tension: 0.4,
          pointRadius: 5,
          pointBackgroundColor: this.colors.primary,
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: {
            beginAtZero: true,
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: { grid: { display: false } }
        }
      }
    });
  }

  _getChartOptions() {
    return {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '65%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 16,
            font: { family: 'Inter', size: 12, weight: '500' },
            usePointStyle: true,
            pointStyle: 'circle'
          }
        }
      }
    };
  }

  _generateHeatmap() {
    const container = document.getElementById('heatmap');
    if (!container) return;

    const today = new Date();
    const days = 365;

    // Limpa o container antes de renderizar para evitar duplicações assíncronas
    container.innerHTML = '';

    for (let i = days; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);

      const cell = document.createElement('div');
      cell.className = 'heatmap-cell';

      const random = Math.random();
      let level = 0;
      if (random > 0.9) level = 4;
      else if (random > 0.75) level = 3;
      else if (random > 0.5) level = 2;
      else if (random > 0.3) level = 1;

      cell.classList.add(`level-${level}`);
      cell.title = `${date.toLocaleDateString('pt-BR')}: ${level} tarefas`;

      container.appendChild(cell);
    }
  }
}

export const Reports = new ReportsManager();