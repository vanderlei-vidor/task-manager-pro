// ========================================
// GERENCIAMENTO DE LOCAL STORAGE
// ========================================

export const Storage = {
  get(key, defaultValue = null) {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : defaultValue;
    } catch (error) {
      console.error(`Erro ao ler storage "${key}":`, error);
      return defaultValue;
    }
  },

  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
      return true;
    } catch (error) {
      console.error(`Erro ao salvar storage "${key}":`, error);
      return false;
    }
  },

  remove(key) {
    try {
      localStorage.removeItem(key);
      return true;
    } catch (error) {
      console.error(`Erro ao remover storage "${key}":`, error);
      return false;
    }
  },

  clear() {
    try {
      localStorage.clear();
      return true;
    } catch (error) {
      console.error('Erro ao limpar storage:', error);
      return false;
    }
  },

  // ========================================
  // 🔐 HELPERS DE TOKEN (string pura — sem JSON.stringify)
  // ========================================
  // Tokens são strings, não objetos. O get/set genérico faz JSON.parse/stringify,
  // o que quebraria um JWT. Esses helpers tratam o token como string bruta.

  getToken(key) {
    try {
      return localStorage.getItem(key);
    } catch (error) {
      console.error(`Erro ao ler token "${key}":`, error);
      return null;
    }
  },

  setToken(key, token) {
    try {
      localStorage.setItem(key, token);
      return true;
    } catch (error) {
      console.error(`Erro ao salvar token "${key}":`, error);
      return false;
    }
  },

  removeToken(key) {
    try {
      localStorage.removeItem(key);
      return true;
    } catch (error) {
      console.error(`Erro ao remover token "${key}":`, error);
      return false;
    }
  }
};

// Chaves pré-definidas (evita typos)
export const StorageKeys = {
  THEME: 'theme',
  SIDEBAR_COLLAPSED: 'sidebarCollapsed',
  KANBAN_FILTER: 'kanbanFilter',
  CALENDAR_VIEW: 'calendarView',
  // 🔐 Tokens de autenticação (strings puras — sem JSON.stringify)
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken'
};