// ========================================
// SISTEMA DE TOAST NOTIFICATIONS
// ========================================
import { Config } from './config.js';

class ToastManager {
    constructor() {
        this.container = null;
        this.icons = {
            success: 'bi-check-circle-fill',
            error: 'bi-x-circle-fill',
            warning: 'bi-exclamation-triangle-fill',
            info: 'bi-info-circle-fill'
        };
    }

    init() {
        if (!document.querySelector('.toast-container')) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        } else {
            this.container = document.querySelector('.toast-container');
        }
    }

    show(type, title, message, duration = Config.toast[type] || 5000) {
        if (!this.container) this.init();

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = this._buildToastHTML(type, title, message);

        // 🔥 Adicionado: Captura o botão dentro do toast recém-criado e amarra o evento de clique
        const closeBtn = toast.querySelector('.toast-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.remove(toast));
        }

        this.container.appendChild(toast);

        // Auto-remove após duração
        setTimeout(() => this.remove(toast), duration);

        return toast;
    }

    remove(toast) {
        if (!toast || toast.classList.contains('removing')) return;

        toast.classList.add('removing');
        setTimeout(() => toast.remove(), 300);
    }

    // Métodos de atalho
    success(title, message) {
        return this.show('success', title, message);
    }

    error(title, message) {
        return this.show('error', title, message, Config.toast.error);
    }

    warning(title, message) {
        return this.show('warning', title, message, Config.toast.warning);
    }

    info(title, message) {
        return this.show('info', title, message);
    }

    // Método privado para construir HTML
    _buildToastHTML(type, title, message) {
        return `
      <div class="toast-icon">
        <i class="bi ${this.icons[type]}"></i>
      </div>
      <div class="toast-content">
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
      </div>
      <button class="toast-close" aria-label="Fechar">
        <i class="bi bi-x"></i>
      </button>
      <div class="toast-progress"></div>
    `;
    }
}

// Exporta instância única (Singleton)
export const Toast = new ToastManager();

// Torna global para uso em HTML inline (se necessário)
window.Toast = Toast;