// ========================================
// GESTOR PRO - ANALYTICS & DASHBOARD
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    // Captura paleta de cores CSS em tempo de execução
    const getThemeColor = (variableName, fallback) => {
        return getComputedStyle(document.documentElement).getPropertyValue(variableName).trim() || fallback;
    };

    const colors = {
        primary: '#6366f1',
        success: '#10b981',
        warning: '#f59e0b',
        danger: '#ef4444',
        info: '#06b6d4',
        text: getThemeColor('--text-primary', '#1e293b'),
        border: getThemeColor('--border-color', 'rgba(0,0,0,0.05)')
    };

    // --- 1. PROGRESS RING RADIAL ---
    const progressRing = document.querySelector('.progress-ring-fill');
    if (progressRing) {
        const percent = parseFloat(progressRing.dataset.percent) || 0;
        const circumference = 2 * Math.PI * 85; // Raio de 85px
        
        progressRing.style.strokeDasharray = circumference;
        progressRing.style.strokeDashoffset = circumference;
        
        setTimeout(() => {
            progressRing.style.strokeDashoffset = circumference - (percent / 100) * circumference;
        }, 150);
        
        const valueEl = document.querySelector('.progress-ring-value');
        if (valueEl && window.GestorUtils) {
            window.GestorUtils.animateCounter(valueEl, percent, 1200);
        }
    }

    // Validador global para instâncias do ChartJS
    if (!window.Chart) return;

    // Configuração de Fonte Padrão Global das tabelas do Chart.js
    Chart.defaults.font.family = 'Inter, sans-serif';
    Chart.defaults.color = colors.text;

    // --- 2. GRÁFICO DE STATUS (DOUGHNUT) ---
    const chartStatusEl = document.getElementById('chartStatus');
    if (chartStatusEl) {
        let statusData = [0, 0, 0];
        
        // CORRIGIDO: Parse seguro do JSON injetado pelo dataset do backend
        try {
            const rawData = JSON.parse(chartStatusEl.dataset.status);
            statusData = [
                parseInt(rawData.DONE || rawData.concluidas) || 0,
                parseInt(rawData.DOING || rawData.andamento) || 0,
                parseInt(rawData.TODO || rawData.pendentes) || 0
            ];
        } catch (e) {
            console.warn("⚠️ Falha ao ler os dados reais de status. Usando mock zeros.");
        }

        new Chart(chartStatusEl.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Concluídas', 'Em Andamento', 'Pendentes'],
                datasets: [{
                    data: statusData,
                    backgroundColor: [colors.success, colors.info, colors.warning],
                    borderWidth: 0,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { padding: 20, usePointStyle: true, pointStyle: 'circle' }
                    }
                }
            }
        });
    }

    // --- 3. GRÁFICO DE PRIORIDADE (BAR) ---
    const chartPriorityEl = document.getElementById('chartPriority');
    if (chartPriorityEl) {
        new Chart(chartPriorityEl.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['Alta', 'Média', 'Baixa'],
                datasets: [{
                    data: [3, 5, 2], // Dados estruturais do escopo
                    backgroundColor: [colors.danger, colors.warning, colors.success],
                    borderRadius: 6,
                    maxBarThickness: 32
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: colors.border } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // --- 4. GRÁFICO LINHA DO TEMPO (LINE) ---
    const chartDayEl = document.getElementById('chartDayOfWeek');
    if (chartDayEl) {
        new Chart(chartDayEl.getContext('2d'), {
            type: 'line',
            data: {
                labels: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'],
                datasets: [{
                    label: 'Tarefas Finalizadas',
                    data: [5, 8, 6, 9, 7, 3, 2],
                    borderColor: colors.primary,
                    backgroundColor: 'rgba(99, 102, 241, 0.08)',
                    fill: true,
                    tension: 0.38,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointBackgroundColor: colors.primary,
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: colors.border } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // --- 5. RENDERIZADOR DO HEATMAP (ESTILO GITHUB) ---
    const heatmapContainer = document.getElementById('heatmap');
    if (heatmapContainer) {
        const fragment = document.createDocumentFragment(); // PERFORMANCE: Injeta os 365 dias de uma vez só!
        const today = new Date();
        const daysToRender = 364; // Exibe exatamente o ciclo de semanas completas

        for (let i = daysToRender; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(date.getDate() - i);

            const cell = document.createElement('div');
            cell.className = 'heatmap-cell';

            // Simulação matemática de produtividade real
            const randomFactor = Math.random();
            let level = 0;
            if (randomFactor > 0.92) level = 4;
            else if (randomFactor > 0.80) level = 3;
            else if (randomFactor > 0.60) level = 2;
            else if (randomFactor > 0.35) level = 1;

            if (level > 0) cell.classList.add(`level-${level}`);
            cell.setAttribute('data-aria-label', `${date.toLocaleDateString('pt-BR')}: ${level} entregas`);
            
            // Tooltip nativo leve
            cell.title = `${date.toLocaleDateString('pt-BR')}: ${level} tarefas`;

            fragment.appendChild(cell);
        }
        heatmapContainer.appendChild(fragment);
    }

    console.log('📊 Painel de Relatórios inicializado com sucesso.');
});