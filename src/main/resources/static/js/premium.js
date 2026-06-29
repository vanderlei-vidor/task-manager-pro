// ========================================
// GESTOR PRO - PREMIUM JAVASCRIPT
// ========================================

document.addEventListener('DOMContentLoaded', function () {

    // ========================================
    // SISTEMA DE TOAST NOTIFICATIONS
    // ========================================
    const Toast = {
        container: null,

        init() {
            // Cria container se não existir
            if (!document.querySelector('.toast-container')) {
                this.container = document.createElement('div');
                this.container.className = 'toast-container';
                document.body.appendChild(this.container);
            } else {
                this.container = document.querySelector('.toast-container');
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
                <div class="toast-icon">
                    <i class="bi ${icons[type]}"></i>
                </div>
                <div class="toast-content">
                    <div class="toast-title">${title}</div>
                    <div class="toast-message">${message}</div>
                </div>
                <button class="toast-close" onclick="Toast.remove(this.parentElement)">
                    <i class="bi bi-x"></i>
                </button>
                <div class="toast-progress"></div>
            `;

            this.container.appendChild(toast);

            // Auto-remove após duração
            setTimeout(() => {
                this.remove(toast);
            }, duration);
        },

        remove(toast) {
            if (!toast || toast.classList.contains('removing')) return;

            toast.classList.add('removing');
            setTimeout(() => {
                toast.remove();
            }, 300);
        },

        success(title, message) { this.show('success', title, message); },
        error(title, message) { this.show('error', title, message, 7000); },
        warning(title, message) { this.show('warning', title, message, 6000); },
        info(title, message) { this.show('info', title, message); }
    };

    // Torna Toast global
    window.Toast = Toast;
    Toast.init();

    // ========================================
    // CONVERTER FLASH MESSAGES EM TOASTS
    // ========================================
    const flashMessages = document.querySelectorAll('.alert-premium');
    flashMessages.forEach(alert => {
        const message = alert.textContent.trim();

        if (alert.classList.contains('alert-success')) {
            Toast.success('Sucesso!', message);
        } else if (alert.classList.contains('alert-danger')) {
            Toast.error('Erro!', message);
        } else if (alert.classList.contains('alert-warning')) {
            Toast.warning('Atenção!', message);
        } else {
            Toast.info('Info', message);
        }

        // Remove o alert original
        alert.remove();
    });

    // ========================================
    // MENU MOBILE
    // ========================================
    const btnMenuMobile = document.getElementById('btnMenuMobile');
    const sidebar = document.querySelector('.sidebar');

    if (btnMenuMobile) {
        btnMenuMobile.addEventListener('click', function () {
            sidebar.classList.toggle('active');
        });
    }

    document.addEventListener('click', function (e) {
        if (window.innerWidth <= 992) {
            if (sidebar && !sidebar.contains(e.target) && btnMenuMobile && !btnMenuMobile.contains(e.target)) {
                sidebar.classList.remove('active');
            }
        }
    });

    // ========================================
    // MODO ESCURO
    // ========================================
    const btnTheme = document.getElementById('btnTheme');

    // Carregar tema salvo
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        if (btnTheme) {
            const icon = btnTheme.querySelector('i');
            if (icon) {
                icon.classList.remove('bi-moon-fill');
                icon.classList.add('bi-sun-fill');
            }
        }
    }

    if (btnTheme) {
        btnTheme.addEventListener('click', function () {
            document.body.classList.toggle('dark-mode');
            const icon = this.querySelector('i');

            if (document.body.classList.contains('dark-mode')) {
                icon.classList.remove('bi-moon-fill');
                icon.classList.add('bi-sun-fill');
                localStorage.setItem('theme', 'dark');
                Toast.success('Tema alterado', 'Modo escuro ativado 🌙');
            } else {
                icon.classList.remove('bi-sun-fill');
                icon.classList.add('bi-moon-fill');
                localStorage.setItem('theme', 'light');
                Toast.success('Tema alterado', 'Modo claro ativado ☀️');
            }
        });
    }

    // ========================================
    // BUSCA GLOBAL
    // ========================================
    const globalSearch = document.getElementById('globalSearch');
    if (globalSearch) {
        globalSearch.addEventListener('input', function (e) {
            const query = e.target.value.toLowerCase();
            const taskRows = document.querySelectorAll('tbody tr');

            taskRows.forEach(row => {
                const title = row.querySelector('td:first-child')?.textContent.toLowerCase() || '';
                if (title.includes(query)) {
                    row.style.display = '';
                    row.style.animation = 'fadeIn 0.2s ease-out';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // ========================================
    // ANIMAÇÃO DE ENTRADA DOS CARDS
    // ========================================
    const cards = document.querySelectorAll('.stat-card, .card-premium');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';

        setTimeout(() => {
            card.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 50);
    });

    // ========================================
    // ANIMAÇÃO DA BARRA DE PROGRESSO
    // ========================================
    const percentageValue = document.querySelector('.percentage-value');
    const progressBar = document.querySelector('.progress-bar-fill');

    if (percentageValue && progressBar) {
        const target = parseInt(percentageValue.textContent);
        let current = 0;
        const increment = target / 30;
        const duration = 1500;
        const stepTime = duration / 30;

        const counter = setInterval(() => {
            current += increment;
            if (current >= target) {
                percentageValue.textContent = target;
                progressBar.style.width = target + '%';
                clearInterval(counter);
            } else {
                percentageValue.textContent = Math.floor(current);
                progressBar.style.width = current + '%';
            }
        }, stepTime);
    }

    // ========================================
    // FILTROS DAS TASKS (Kanban)
    // ========================================
    const filterButtons = document.querySelectorAll('.filter-btn');
    const taskCards = document.querySelectorAll('.task-card');

    if (filterButtons.length > 0) {
        filterButtons.forEach(btn => {
            btn.addEventListener('click', function () {
                filterButtons.forEach(b => b.classList.remove('active'));
                this.classList.add('active');

                const filter = this.dataset.filter;

                taskCards.forEach(card => {
                    const priority = card.dataset.priority;
                    const isOverdue = card.classList.contains('card-overdue');

                    let show = false;

                    if (filter === 'all') {
                        show = true;
                    } else if (filter === 'overdue') {
                        show = isOverdue;
                    } else {
                        show = priority === filter;
                    }

                    if (show) {
                        card.style.display = 'flex';
                        card.style.animation = 'slideInUp 0.3s ease-out';
                    } else {
                        card.style.display = 'none';
                    }
                });
            });
        });
    }

    // ========================================
    // CONTADORES ANIMADOS
    // ========================================
    function animateCounter(element, target, duration = 1000) {
        const start = 0;
        const increment = target / (duration / 16);
        let current = start;

        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                element.textContent = target;
                clearInterval(timer);
            } else {
                element.textContent = Math.floor(current);
            }
        }, 16);
    }

    const columnCounters = document.querySelectorAll('.column-count');
    columnCounters.forEach(counter => {
        const target = parseInt(counter.textContent);
        animateCounter(counter, target, 800);
    });

    // ========================================
    // CALENDÁRIO - NAVEGAÇÃO
    // ========================================
    if (window.location.pathname === '/calendario') {
        console.log('📅 Calendário detectado!');

        const btnMesAnterior = document.getElementById('btnMesAnterior');
        const btnMesProximo = document.getElementById('btnMesProximo');
        const btnHoje = document.getElementById('btnHoje');

        const urlParams = new URLSearchParams(window.location.search);
        let mesAtual = parseInt(urlParams.get('mes')) || new Date().getMonth() + 1;
        let anoAtual = parseInt(urlParams.get('ano')) || new Date().getFullYear();

        if (btnMesAnterior) {
            btnMesAnterior.addEventListener('click', function (e) {
                e.preventDefault();
                mesAtual--;
                if (mesAtual < 1) {
                    mesAtual = 12;
                    anoAtual--;
                }
                window.location.href = `/calendario?mes=${mesAtual}&ano=${anoAtual}`;
            });
        }

        if (btnMesProximo) {
            btnMesProximo.addEventListener('click', function (e) {
                e.preventDefault();
                mesAtual++;
                if (mesAtual > 12) {
                    mesAtual = 1;
                    anoAtual++;
                }
                window.location.href = `/calendario?mes=${mesAtual}&ano=${anoAtual}`;
            });
        }

        if (btnHoje) {
            btnHoje.addEventListener('click', function (e) {
                e.preventDefault();
                const hoje = new Date();
                window.location.href = `/calendario?mes=${hoje.getMonth() + 1}&ano=${hoje.getFullYear()}`;
            });
        }
    }

    // ========================================
    // ATALHOS DE TECLADO GLOBAIS
    // ========================================
    document.addEventListener('keydown', function (e) {
        // Ignora se estiver digitando em input/textarea
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.isContentEditable) {
            return;
        }

        // Ctrl + N = Nova tarefa (abre modal)
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
            e.preventDefault();
            const modalNovaTarefa = document.getElementById('modalNovaTarefa');
            if (modalNovaTarefa) {
                const modal = new bootstrap.Modal(modalNovaTarefa);
                modal.show();
                Toast.info('Atalho', 'Modal de nova tarefa aberto');
            }
        }

        // Ctrl + K = Foco na busca
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchBox = document.getElementById('globalSearch');
            if (searchBox) {
                searchBox.focus();
                Toast.info('Busca', 'Foco na busca global');
            }
        }

        // Ctrl + D = Toggle modo escuro
        if ((e.ctrlKey || e.metaKey) && e.key === 'd') {
            e.preventDefault();
            if (btnTheme) {
                btnTheme.click();
            }
        }

        // Esc = Fecha modais
        if (e.key === 'Escape') {
            const openModals = document.querySelectorAll('.modal.show');
            openModals.forEach(modal => {
                const bsModal = bootstrap.Modal.getInstance(modal);
                if (bsModal) bsModal.hide();
            });
        }

        // ? = Mostra atalhos
        if (e.key === '?') {
            showShortcutsHelp();
        }

        // Navegação no calendário
        if (window.location.pathname === '/calendario') {
            const btnMesAnterior = document.getElementById('btnMesAnterior');
            const btnMesProximo = document.getElementById('btnMesProximo');

            if (e.key === 'ArrowLeft' && btnMesAnterior) {
                btnMesAnterior.click();
            } else if (e.key === 'ArrowRight' && btnMesProximo) {
                btnMesProximo.click();
            }
        }
    });

    // ========================================
    // AJUDA DE ATALHOS
    // ========================================
    function showShortcutsHelp() {
        const shortcuts = [
            { keys: ['Ctrl', 'N'], label: 'Nova tarefa' },
            { keys: ['Ctrl', 'K'], label: 'Busca rápida' },
            { keys: ['Ctrl', 'D'], label: 'Modo escuro' },
            { keys: ['Esc'], label: 'Fechar modal' },
            { keys: ['←', '→'], label: 'Navegar calendário' },
            { keys: ['?'], label: 'Esta ajuda' }
        ];

        let message = shortcuts.map(s =>
            `<div style="display:flex;align-items:center;gap:8px;margin:4px 0;">
                ${s.keys.map(k => `<span class="shortcut-key">${k}</span>`).join('')}
                <span style="color:var(--text-secondary);font-size:13px;">${s.label}</span>
            </div>`
        ).join('');

        Toast.info('Atalhos de Teclado', message, 8000);
    }

    // ========================================
    // BOTÃO DE AJUDA
    // ========================================
    const btnHelp = document.createElement('button');
    btnHelp.className = 'btn-help';
    btnHelp.innerHTML = '<i class="bi bi-question-lg"></i>';
    btnHelp.title = 'Atalhos de teclado (?)';
    btnHelp.onclick = showShortcutsHelp;
    document.body.appendChild(btnHelp);

    // ========================================
    // TOAST DE BOAS-VINDAS
    // ========================================
    setTimeout(() => {
        const nomeUsuario = document.querySelector('.user-name')?.textContent || 'Usuário';
        Toast.success(`Bem-vindo, ${nomeUsuario}! 👋`, 'Pressione ? para ver os atalhos');
    }, 500);


    // ========================================
    // CONVERTER NOVAS FLASH MESSAGES
    // ========================================
    const mensagensSucesso = document.querySelectorAll('[th\\:if="${mensagemSucesso}"]');
    const mensagensErro = document.querySelectorAll('[th\\:if="${mensagemErro}"]');

    // O Thymeleaf já processou, então procuramos por divs com as classes
    const alertSuccess = document.querySelector('.alert-success');
    const alertDanger = document.querySelector('.alert-danger');

    if (alertSuccess) {
        Toast.success('Sucesso!', alertSuccess.textContent.trim());
        alertSuccess.remove();
    }

    if (alertDanger) {
        Toast.error('Erro!', alertDanger.textContent.trim());
        alertDanger.remove();
    }

    // ========================================
    // FIX: GARANTIR QUE INPUTS DO MODAL FUNCIONEM
    // ========================================
    document.addEventListener('shown.bs.modal', function (event) {
        const modal = event.target;

        // Forçar foco nos inputs
        const inputs = modal.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            input.style.pointerEvents = 'auto';
            input.style.position = 'relative';
            input.style.zIndex = '10';
        });

        // Garantir que labels das tags sejam clicáveis
        const tagLabels = modal.querySelectorAll('.tag-label');
        tagLabels.forEach(label => {
            label.style.pointerEvents = 'auto';
            label.style.cursor = 'pointer';
        });

        console.log('🎯 Modal corrigido para inputs!');
    });

    // Fix para tags - garantir que o clique funcione
    document.addEventListener('click', function (e) {
        if (e.target.classList.contains('tag-label')) {
            const forAttr = e.target.getAttribute('for');
            if (forAttr) {
                const checkbox = document.getElementById(forAttr);
                if (checkbox) {
                    checkbox.checked = !checkbox.checked;
                    console.log('✅ Tag selecionada:', checkbox.value);
                }
            }
        }
    });

    // ========================================
    // PREVIEW DE TAG NO MODAL
    // ========================================
    const colorPicker = document.querySelector('.color-picker');
    const tagPreview = document.getElementById('tagPreview');
    const tagPreviewDot = document.getElementById('tagPreviewDot');
    const tagPreviewName = document.getElementById('tagPreviewName');
    const tagNameInput = document.querySelector('input[name="name"]');

    if (colorPicker && tagPreview) {
        colorPicker.addEventListener('input', function (e) {
            if (tagPreviewDot) {
                tagPreviewDot.style.background = e.target.value;
            }
        });
    }

    if (tagNameInput && tagPreviewName) {
        tagNameInput.addEventListener('input', function (e) {
            tagPreviewName.textContent = e.target.value || 'Nome da Tag';
        });
    }

    // ========================================
    // ATUALIZAR SIDEBAR
    // ========================================
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    const currentPage = document.querySelector('[data-current-page]');
    if (currentPage) {
        const page = currentPage.getAttribute('data-current-page');
        const navItem = document.querySelector(`.nav-item[href="/${page}"]`);
        if (navItem) {
            navItem.classList.add('active');
        }
    }

    console.log('🏷️ Página de tags carregada!');

    console.log('🚀 Gestor Pro Premium carregado com sucesso!');
});