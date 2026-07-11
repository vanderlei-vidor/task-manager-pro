// ========================================
// 👋 LOGOUT MANAGER - GESTOR PRO (Versão Blindada)
// ========================================
import { Toast } from '../core/toast.js';
import { $ } from '../core/utils.js';
import { Config } from '../core/config.js';
import { clearTokens, getAccessToken } from '../core/api.js';

class LogoutManager {
    constructor() {
        this.logoutForms = [];
    }

    init() {
        this.logoutForms = Array.from(document.querySelectorAll('form[action*="logout"]'));

        if (this.logoutForms.length === 0) {
            console.warn('⚠️ Nenhum form de logout encontrado');
            return;
        }

        console.log('👋 Logout Manager inicializado com', this.logoutForms.length, 'form(s)');
        this._bindEvents();
    }

    _bindEvents() {
        this.logoutForms.forEach(form => {
            // Remove listeners antigos para evitar duplicação
            const newForm = form.cloneNode(true);
            form.parentNode.replaceChild(newForm, form);

            newForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this._handleLogout(newForm);
            });
        });
    }

    _handleLogout(form) {
        const userName = $('.user-name')?.textContent.trim() || 'Usuário';

        console.log('👋 Iniciando logout de:', userName);

        // 🎯 Toast de despedida
        Toast.info(
            `👋 Até logo, ${userName}!`,
            'Você será desconectado em instantes...',
            1500,
            'toast-logout'
        );

        // 🎨 Efeito visual: fade out
        const sidebar = $('.sidebar');
        const mainContent = $('.main-content');

        if (sidebar) {
            sidebar.style.transition = 'opacity 0.6s ease';
            sidebar.style.opacity = '0.3';
        }

        if (mainContent) {
            mainContent.style.transition = 'opacity 0.6s ease';
            mainContent.style.opacity = '0.3';
        }

        // ⏳ Aguarda e submete
        setTimeout(() => {
            console.log('👋 Submetendo form de logout...');

            // 🔐 Limpa tokens do localStorage ANTES de submeter o form.
            // Antes o logout só destruía a sessão SSR (cookie), deixando os
            // tokens JWT no localStorage — vulneráveis a XSS após logout.
            this._cleanupTokens();

            form.submit();
        }, 1000);
    }

    /**
     * 🔐 Limpa os tokens de autenticação do storage.
     *
     * Best-effort: tenta revogar o refresh token no backend (fire-and-forget)
     * e SEMPRE limpa os tokens do localStorage, mesmo se o fetch falhar.
     */
    _cleanupTokens() {
        const token = getAccessToken();

        // Best-effort: avisa o backend para revogar os refresh tokens.
        // Não aguardamos a resposta — o logout SSR (form.submit()) já derruba a sessão,
        // e não queremos atrasar a navegação do usuário.
        if (token) {
            try {
                fetch(Config.auth.logoutUrl, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }).catch(err => console.warn('Aviso: logout no backend falhou (tokens locais foram limpos):', err));
            } catch (err) {
                // Ignora — o importante é limpar o storage local
            }
        }

        // Garante a limpeza local em TODOS os casos
        clearTokens();
        console.log('🔐 Tokens locais limpos após logout.');
    }
}

export const Logout = new LogoutManager();