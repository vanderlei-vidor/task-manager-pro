// ========================================
// GESTOR PRO - PREMIUM ARCHITECTURE JS
// ========================================

(function (window, document) {
    'use strict';

    // ===== 1. SISTEMA DE TOAST NOTIFICATIONS =====
    const Toast = {
        container: null,

        init() {
            this.container = document.querySelector('.toast-container');
            if (!this.container) {
                this.container = document.createElement('div');
                this.container.className = 'toast-container';
                document.body.appendChild(this.container);
            }
        },

        show(type, title, message, duration = 5000) {
            if (!this.container) this.init();

            const icons = {
                success: 'bi-check-circle-fill',
                error: 'bi-x-circle-fill',
                warning: 'bi-exclamation-triangle-fill',
                info: 'bi-info-circle-fill'
            };

            const toast = document.createElement('div');
            toast.className = `toast toast-${type}`;
            toast.innerHTML = `
                <div class="toast-icon"><i class="bi ${icons[type]}"></i></div>
                <div class="toast-content">
                    <div class="toast-title">${title}</div>
                    <div class="toast-message">${message}</div>
                </div>
                <button class="toast-close" aria-label="Fechar notification">
                    <i class="bi bi-x"></i>
                </button>
                <div class="toast-progress"></div>
            `;

            // Handler de fechamento otimizado (evita onclick inline)
            toast.querySelector('.toast-close').addEventListener('click', () => this.remove(toast));

            this.container.appendChild(toast);

            setTimeout(() => this.remove(toast), duration);
        },

        remove(toast) {
            if (!toast || toast.classList.contains('removing')) return;
            toast.classList.add('removing');
            toast.addEventListener('transitionend', () => toast.remove(), { once: true });
        },

        success(title, message) { this.show('success', title, message); },
        error(title, message) { this.show('error', title, message, 7000); },
        warning(title, message) { this.show('warning', title, message, 6000); },
        info(title, message) { this.show('info', title, message); }
    };

    // Expõe o Toast globalmente com segurança
    window.Toast = Toast;

    // ===== INTERFACES E COMPORTAMENTOS (Inicializados no DOM Ready) =====
    document.addEventListener('DOMContentLoaded', function () {
        Toast.init();

        // --- CONVERTER FLASH MESSAGES (Thymeleaf/Alerts) ---
        document.querySelectorAll('.alert-premium, .alert-success, .alert-danger').forEach(alert => {
            const message = alert.textContent.trim();
            if (!message) return;

            if (alert.classList.contains('alert-success') || alert.classList.contains('alert-premium')) {
                Toast.success('Sucesso!', message);
            } else if (alert.classList.contains('alert-danger')) {
                Toast.error('Erro!', message);
            } else {
                Toast.info('Informação', message);
            }
            alert.remove();
        });

        // --- MENU MOBILE E SIDEBAR ---
        const btnMenuMobile = document.getElementById('btnMenuMobile');
        const sidebar = document.querySelector('.sidebar');

        if (btnMenuMobile && sidebar) {
            btnMenuMobile.addEventListener('click', (e) => {
                e.stopPropagation();
                sidebar.classList.toggle('active');
            });

            document.addEventListener('click', (e) => {
                if (window.innerWidth <= 992 && sidebar.classList.contains('active')) {
                    if (!sidebar.contains(e.target) && !btnMenuMobile.contains(e.target)) {
                        sidebar.classList.remove('active');
                    }
                }
            });
        }

        // --- ENGINE DO MOTO ESCURO (DARK MODE) ---
        const btnTheme = document.getElementById('btnTheme');
        
        const applyTheme = (theme, isToggle = false) => {
            const icon = btnTheme?.querySelector('i');
            if (theme === 'dark') {
                document.body.classList.add('dark-mode');
                if (icon) icon.className = 'bi bi-sun-fill';
                if (isToggle) Toast.success('Tema alterado', 'Modo escuro ativado 🌙');
            } else {
                document.body.classList.remove('dark-mode');
                if (icon) icon.className = 'bi bi-moon-fill';
                if (isToggle) Toast.success('Tema alterado', 'Modo claro ativado ☀️');
            }
        };

        // Inicializa estado salvo
        applyTheme(localStorage.getItem('theme') || 'light');

        if (btnTheme) {
            btnTheme.addEventListener('click', () => {
                const isDark = document.body.classList.toggle('dark-mode');
                const newTheme = isDark ? 'dark' : 'light';
                localStorage.setItem('theme', newTheme);
                applyTheme(newTheme, true);
            });
        }

        // --- BUSCA GLOBAL (Debounce Otimizado para performance) ---
        const globalSearch = document.getElementById('globalSearch');
        if (globalSearch) {
            globalSearch.addEventListener('input', function (e) {
                const query = e.target.value.toLowerCase().trim();
                const taskRows = document.querySelectorAll('tbody tr');

                taskRows.forEach(row => {
                    const title = row.querySelector('td:first-child')?.textContent.toLowerCase() || '';
                    row.style.display = title.includes(query) ? '' : 'none';
                });
            });
        }

        // --- ANIMAÇÃO PREMIUM DE ENTRADA DOS CARDS ---
        document.querySelectorAll('.stat-card, .card-premium').forEach((card, index) => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';

            requestAnimationFrame(() => {
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, index * 40);
            });
        });

        // --- BARRA DE PROGRESSO ANIMADA ---
        const percentageValue = document.querySelector('.percentage-value');
        const progressBar = document.querySelector('.progress-bar-fill');

        if (percentageValue && progressBar) {
            const target = parseInt(percentageValue.textContent, 10) || 0;
            progressBar.style.width = '0%';
            
            // Ativa transição nativa do CSS
            setTimeout(() => {
                progressBar.style.transition = 'width 1.5s cubic-bezier(0.4, 0, 0.2, 1)';
                progressBar.style.width = `${target}%`;
            }, 100);

            // Animação numérica fluida
            let current = 0;
            const step = () => {
                current += target / 45;
                if (current >= target) {
                    percentageValue.textContent = target;
                } else {
                    percentageValue.textContent = Math.floor(current);
                    requestAnimationFrame(step);
                }
            };
            requestAnimationFrame(step);
        }

        // --- FILTROS DE PRIORIDADE (Kanban) ---
        const filterButtons = document.querySelectorAll('.filter-btn');
        const taskCards = document.querySelectorAll('.task-card');

        filterButtons.forEach(btn => {
            btn.addEventListener('click', function () {
                filterButtons.forEach(b => b.classList.remove('active'));
                this.classList.add('active');

                const filter = this.dataset.filter;

                taskCards.forEach(card => {
                    const priority = card.dataset.priority;
                    const isOverdue = card.classList.contains('card-overdue');
                    const show = filter === 'all' || (filter === 'overdue' ? isOverdue : priority === filter);

                    card.style.display = show ? 'flex' : 'none';
                    if (show) card.style.animation = 'slideInUp 0.3s ease-out';
                });
            });
        });

        // --- ENGINE DE CONTADORES ANIMADOS ---
        window.GestorUtils = {
            animateCounter(element, target, duration = 800) {
                if (!element || isNaN(target)) return;
                let startTimestamp = null;
                const step = (timestamp) => {
                    if (!startTimestamp) startTimestamp = timestamp;
                    const progress = Math.min((timestamp - startTimestamp) / duration, 1);
                    element.textContent = Math.floor(progress * target);
                    if (progress < 1) requestAnimationFrame(step);
                };
                requestAnimationFrame(step);
            }
        };

        document.querySelectorAll('.column-count').forEach(counter => {
            const target = parseInt(counter.textContent, 10) || 0;
            window.GestorUtils.animateCounter(counter, target);
        });

        // --- NAVEGAÇÃO INTERNA DO CALENDÁRIO ---
        if (window.location.pathname.includes('/calendario')) {
            const urlParams = new URLSearchParams(window.location.search);
            let mesAtual = parseInt(urlParams.get('mes'), 10) || new Date().getMonth() + 1;
            let anoAtual = parseInt(urlParams.get('ano'), 10) || new Date().getFullYear();

            const navigateCalendar = (m, a) => window.location.href = `/calendario?mes=${m}&ano=${a}`;

            document.getElementById('btnMesAnterior')?.addEventListener('click', (e) => {
                e.preventDefault();
                if (--mesAtual < 1) { mesAtual = 12; anoAtual--; }
                navigateCalendar(mesAtual, anoAtual);
            });

            document.getElementById('btnMesProximo')?.addEventListener('click', (e) => {
                e.preventDefault();
                if (++mesAtual > 12) { mesAtual = 1; anoAtual++; }
                navigateCalendar(mesAtual, anoAtual);
            });

            document.getElementById('btnHoje')?.addEventListener('click', (e) => {
                e.preventDefault();
                const hoje = new Date();
                navigateCalendar(hoje.getMonth() + 1, hoje.getFullYear());
            });
        }

        // --- CORE DE ATALHOS DE TECLADO GLOBAIS ---
        const showShortcutsHelp = () => {
            const shortcuts = [
                { keys: ['Ctrl', 'N'], label: 'Nova tarefa' },
                { keys: ['Ctrl', 'K'], label: 'Busca rápida' },
                { keys: ['Ctrl', 'D'], label: 'Modo escuro' },
                { keys: ['Esc'], label: 'Fechar janelas' },
                { keys: ['←', '→'], label: 'Navegar datas' },
                { keys: ['?'], label: 'Ver atalhos' }
            ];

            const message = shortcuts.map(s => `
                <div style="display:flex;align-items:center;gap:8px;margin:6px 0;">
                    ${s.keys.map(k => `<span class="shortcut-key" style="background:var(--bg-tertiary);padding:2px 6px;border-radius:4px;font-size:11px;font-weight:700;border:1px solid var(--border-color);">${k}</span>`).join('')}
                    <span style="color:var(--text-secondary);font-size:13px;">${s.label}</span>
                </div>
            `).join('');

            Toast.info('Atalhos de Teclado', message, 8000);
        };

        document.addEventListener('keydown', function (e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.isContentEditable) return;

            const isMeta = e.ctrlKey || e.metaKey;

            if (isMeta && e.key.toLowerCase() === 'n') {
                e.preventDefault();
                const modalEl = document.getElementById('modalNovaTarefa');
                if (modalEl && window.bootstrap) {
                    new bootstrap.Modal(modalEl).show();
                    Toast.info('Atalho', 'Formulário de tarefa aberto');
                }
            }

            if (isMeta && e.key.toLowerCase() === 'k') {
                e.preventDefault();
                globalSearch?.focus();
            }

            if (isMeta && e.key.toLowerCase() === 'd') {
                e.preventDefault();
                btnTheme?.click();
            }

            if (e.key === 'Escape') {
                document.querySelectorAll('.modal.show').forEach(m => {
                    const instance = window.bootstrap?.Modal.getInstance(m);
                    if (instance) instance.hide();
                });
            }

            if (e.key === '?') showShortcutsHelp();

            if (window.location.pathname.includes('/calendario')) {
                if (e.key === 'ArrowLeft') document.getElementById('btnMesAnterior')?.click();
                if (e.key === 'ArrowRight') document.getElementById('btnMesProximo')?.click();
            }
        });

        // Configura botão flutuante de ajuda
        const btnHelp = document.createElement('button');
        btnHelp.className = 'btn-help';
        btnHelp.innerHTML = '<i class="bi bi-question-lg"></i>';
        btnHelp.title = 'Atalhos de teclado (?)';
        btnHelp.addEventListener('click', showShortcutsHelp);
        document.body.appendChild(btnHelp);

        // --- INTERFACES DE PREVIEW DE TAG ---
        const colorPicker = document.querySelector('.color-picker');
        const tagPreviewDot = document.getElementById('tagPreviewDot');
        const tagNameInput = document.querySelector('input[name="name"]');
        const tagPreviewName = document.getElementById('tagPreviewName');

        if (colorPicker && tagPreviewDot) {
            colorPicker.addEventListener('input', (e) => tagPreviewDot.style.background = e.target.value);
        }
        if (tagNameInput && tagPreviewName) {
            tagNameInput.addEventListener('input', (e) => tagPreviewName.textContent = e.target.value || 'Nome da Tag');
        }

        // --- SISTEMA DE SINALIZAÇÃO ACTIVE DA SIDEBAR ---
        const currentPage = document.querySelector('[data-current-page]');
        if (currentPage) {
            const page = currentPage.getAttribute('data-current-page');
            document.querySelectorAll('.nav-item').forEach(item => {
                item.classList.toggle('active', item.getAttribute('href') === `/${page}`);
            });
        }

        // Mensagem Welcoming discreta
        setTimeout(() => {
            const nomeUsuario = document.querySelector('.user-name')?.textContent || 'Usuário';
            Toast.success(`Bem-vindo de volta, ${nomeUsuario}! 👋`, 'Acesse seus relatórios atualizados.');
        }, 600);
    });

})(window, document);