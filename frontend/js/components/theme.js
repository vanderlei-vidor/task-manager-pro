// ========================================
// GERENCIAMENTO DE TEMA (DARK/LIGHT)
// ========================================
import { Toast } from '../core/toast.js';
import { Storage, StorageKeys } from '../core/storage.js';
import { $ } from '../core/utils.js';

class ThemeManager {
  constructor() {
    this.btnTheme = $('#btnTheme');
    this.currentTheme = Storage.get(StorageKeys.THEME, 'light');
    this.btnTheme = null;
  }

  init() {
    // 💡 Elemento capturado de forma segura agora que o DOM está pronto
    this.btnTheme = $('#btnTheme');
    
    this._applyTheme(this.currentTheme);
    this._bindEvents();
  }

  _applyTheme(theme) {
    if (theme === 'dark') {
      document.body.classList.add('dark-mode');
      this._updateIcon('sun');
    } else {
      document.body.classList.remove('dark-mode');
      this._updateIcon('moon');
    }
  }

  _updateIcon(iconType) {
    if (!this.btnTheme) return;
    const icon = this.btnTheme.querySelector('i');
    if (!icon) return;

    icon.classList.remove('bi-moon-fill', 'bi-sun-fill');
    icon.classList.add(iconType === 'sun' ? 'bi-sun-fill' : 'bi-moon-fill');
  }

  toggle() {
    const isDark = document.body.classList.toggle('dark-mode');
    const newTheme = isDark ? 'dark' : 'light';

    Storage.set(StorageKeys.THEME, newTheme);
    this._updateIcon(isDark ? 'sun' : 'moon');

    const message = isDark ? 'Modo escuro ativado 🌙' : 'Modo claro ativado ☀️';
    Toast.success('Tema alterado', message);

    this.currentTheme = newTheme;
    return newTheme;
  }

  _bindEvents() {
    if (this.btnTheme) {
      this.btnTheme.addEventListener('click', () => this.toggle());
    }
  }
}

export const Theme = new ThemeManager();