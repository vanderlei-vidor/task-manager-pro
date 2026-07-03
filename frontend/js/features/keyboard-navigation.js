// ========================================
// 🧭 KEYBOARD NAVIGATION - GESTOR PRO
// ========================================
import { Toast } from '../core/toast.js';

class KeyboardNavigationManager {
    constructor() {
        this.firstKey = null;
        this.resetTimeout = null;

        // Mapeamento de rotas
        this.routes = {
            'd': { url: '/', label: 'Dashboard' },
            'k': { url: '/kanban', label: 'Kanban' },
            'c': { url: '/calendario', label: 'Calendário' },
            't': { url: '/tags', label: 'Tags' },
            'r': { url: '/relatorios', label: 'Relatórios' }
        };
    }

    init() {
        console.log('🧭 Keyboard Navigation inicializado');
        this._bindEvents();
    }

    _bindEvents() {
        document.addEventListener('keydown', (e) => {
            // Ignora se estiver digitando em input/textarea
            const isTyping = ['INPUT', 'TEXTAREA', 'SELECT'].includes(e.target.tagName);
            if (isTyping) return;

            // Ignora se modal está aberto
            const modalOpen = document.querySelector('.modal.show');
            if (modalOpen) return;

            // Ignora se Ctrl/Alt/Meta estão pressionados (exceto combinações específicas)
            if (e.ctrlKey || e.metaKey) {
                // Ctrl+K: Buscar
                if (e.key.toLowerCase() === 'k') {
                    e.preventDefault();
                    this._focusSearch();
                    return;
                }

                // Ctrl+D: Toggle tema
                if (e.key.toLowerCase() === 'd') {
                    e.preventDefault();
                    this._toggleTheme();
                    return;
                }

                // Ctrl+Shift+L: Logout
                if (e.shiftKey && e.key.toLowerCase() === 'l') {
                    e.preventDefault();
                    this._logout();
                    return;
                }

                return; // Ignora outras combinações Ctrl
            }

            // ✅ NOVO: Tecla "N" sozinha → Nova tarefa (padrão Trello/Asana)
            if (e.key.toLowerCase() === 'n') {
                e.preventDefault();
                this._openNewTaskModal();
                return;
            }

            // Navegação com "g" + tecla
            if (e.key.toLowerCase() === 'g' && !this.firstKey) {
                this.firstKey = 'g';
                this._startResetTimer();
                Toast.info('🧭 Navegação', 'Pressione a tecla de destino...', 2000);
                return;
            }

            // Segunda tecla após "g"
            if (this.firstKey === 'g' && this.routes[e.key.toLowerCase()]) {
                e.preventDefault();
                const route = this.routes[e.key.toLowerCase()];
                Toast.success(`🚀 Navegando`, `Indo para ${route.label}...`, 2000);

                setTimeout(() => {
                    window.location.href = route.url;
                }, 500);

                this._resetSequence();
                return;
            }

            // Tecla inválida após "g"
            if (this.firstKey === 'g') {
                Toast.warning('⚠️ Atalho inválido', `Tecla "${e.key}" não reconhecida`, 2000);
                this._resetSequence();
            }
        });
    }

    _openNewTaskModal() {
        const modal = document.querySelector('#modalNovaTarefa');
        if (modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
            Toast.info('📝 Nova Tarefa', 'Criando nova tarefa...', 1500);
        }
    }

    _focusSearch() {
        const searchInput = document.querySelector('#globalSearch');
        if (searchInput) {
            searchInput.focus();
            searchInput.select();
            Toast.info('🔍 Busca', 'Digite para buscar...', 1500);
        }
    }

    _toggleTheme() {
        const btnTheme = document.querySelector('#btnTheme');
        if (btnTheme) {
            btnTheme.click();
            Toast.info('🎨 Tema', 'Alternando tema...', 1500);
        }
    }

    _logout() {
        if (confirm('Deseja realmente sair?')) {
            const logoutForm = document.querySelector('form[action*="logout"]');
            if (logoutForm) {
                Toast.info('👋 Logout', 'Até logo!', 1500);
                setTimeout(() => {
                    logoutForm.submit();
                }, 500);
            }
        }
    }

    _startResetTimer() {
        clearTimeout(this.resetTimeout);
        this.resetTimeout = setTimeout(() => {
            this._resetSequence();
        }, 1500); // 1.5 segundos para pressionar a segunda tecla
    }

    _resetSequence() {
        this.firstKey = null;
        clearTimeout(this.resetTimeout);
    }
}

export const KeyboardNavigation = new KeyboardNavigationManager();