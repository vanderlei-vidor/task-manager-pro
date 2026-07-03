// ========================================
// 👋 LOGOUT MANAGER - GESTOR PRO (Versão Blindada)
// ========================================
import { Toast } from '../core/toast.js';
import { $ } from '../core/utils.js';

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
            form.submit();
        }, 1000);
    }
}

export const Logout = new LogoutManager();