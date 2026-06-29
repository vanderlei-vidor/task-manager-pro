// ========================================
// RELATÓRIOS - GRÁFICOS E HEATMAP
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    
    // Cores do tema
    const colors = {
        primary: '#6366f1',
        success: '#10b981',
        warning: '#f59e0b',
        danger: '#ef4444',
        info: '#06b6d4',
        purple: '#8b5cf6',
        bg: getComputedStyle(document.documentElement).getPropertyValue('--bg-secondary').trim() || '#f8fafc'
    };
    
    // ========================================
    // PROGRESS RING ANIMADO
    // ========================================
    const progressRing = document.querySelector('.progress-ring-fill');
    if (progressRing) {
        const percent = parseFloat(progressRing.dataset.percent) || 0;
        const circumference = 2 * Math.PI * 85;
        const offset = circumference - (percent / 100) * circumference;
        
        setTimeout(() => {
            progressRing.style.strokeDashoffset = offset;
        }, 300);
        
        // Animar número
        const valueEl = document.querySelector('.progress-ring-value');
        if (valueEl) {
            animateValue(valueEl, 0, percent, 1500);
        }
    }
    
    // ========================================
    // GRÁFICO DE STATUS (PIZZA)
    // ========================================
    const chartStatusEl = document.getElementById('chartStatus');
    if (chartStatusEl) {
        const ctx = chartStatusEl.getContext('2d');
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Concluídas', 'Em Andamento', 'Pendentes'],
                datasets: [{
                    data: [
                        parseInt(chartStatusEl.dataset.status?.TODO) || 0,
                        parseInt(chartStatusEl.dataset.status?.DOING) || 0,
                        parseInt(chartStatusEl.dataset.status?.DONE) || 0
                    ],
                    backgroundColor: [colors.success, colors.info, colors.warning],
                    borderWidth: 0,
                    hoverOffset: 8
                }]
            },
            options: {
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
            }
        });
    }
    
    // ========================================
    // GRÁFICO DE PRIORIDADE (BARRAS)
    // ========================================
    const chartPriorityEl = document.getElementById('chartPriority');
    if (chartPriorityEl) {
        const ctx = chartPriorityEl.getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Alta', 'Média', 'Baixa'],
                datasets: [{
                    label: 'Tarefas',
                    data: [3, 5, 2], // Valores de exemplo - ajustar com dados reais
                    backgroundColor: [colors.danger, colors.warning, colors.success],
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.05)' },
                        ticks: { font: { family: 'Inter' } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { font: { family: 'Inter', weight: '500' } }
                    }
                }
            }
        });
    }
    
    // ========================================
    // GRÁFICO DIA DA SEMANA (LINHA)
    // ========================================
    const chartDayEl = document.getElementById('chartDayOfWeek');
    if (chartDayEl) {
        const ctx = chartDayEl.getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'],
                datasets: [{
                    label: 'Tarefas',
                    data: [5, 8, 6, 9, 7, 3, 2],
                    borderColor: colors.primary,
                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 5,
                    pointBackgroundColor: colors.primary,
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: {
                        grid: { display: false }
                    }
                }
            }
        });
    }
    
    // ========================================
    // HEATMAP ESTILO GITHUB
    // ========================================
    generateHeatmap();
    
    console.log(' Relatórios carregados!');
});

// ========================================
// FUNÇÕES AUXILIARES
// ========================================

function animateValue(element, start, end, duration) {
    const range = end - start;
    const increment = range / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        if (current >= end) {
            element.textContent = Math.round(end);
            clearInterval(timer);
        } else {
            element.textContent = Math.round(current);
        }
    }, 16);
}

function generateHeatmap() {
    const container = document.getElementById('heatmap');
    if (!container) return;
    
    // Gerar 365 dias (52 semanas x 7 dias)
    const today = new Date();
    const days = 365;
    
    for (let i = days; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        
        const cell = document.createElement('div');
        cell.className = 'heatmap-cell';
        
        // Simular dados (substituir por dados reais do backend)
        const random = Math.random();
        let level = 0;
        if (random > 0.9) level = 4;
        else if (random > 0.75) level = 3;
        else if (random > 0.5) level = 2;
        else if (random > 0.3) level = 1;
        
        cell.classList.add('level-' + level);
        cell.title = `${date.toLocaleDateString('pt-BR')}: ${level} tarefas`;
        
        container.appendChild(cell);
    }
}