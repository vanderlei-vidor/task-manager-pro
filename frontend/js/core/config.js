// ========================================
// CONFIGURAÇÕES GLOBAIS
// ========================================
export const Config = {
  // Cores do tema
  colors: {
    primary: '#6366f1',
    success: '#10b981',
    warning: '#f59e0b',
    danger: '#ef4444',
    info: '#06b6d4',
    purple: '#8b5cf6',
    bg: getComputedStyle(document.documentElement)
      .getPropertyValue('--bg-secondary').trim() || '#f8fafc'
  },

  // Durações de animação (ms)
  animation: {
    fast: 150,
    normal: 300,
    slow: 500
  },

  // Durações de toast (ms)
  toast: {
    success: 5000,
    error: 7000,
    warning: 6000,
    info: 5000
  },

  // Breakpoints (devem bater com o CSS)
  breakpoints: {
    mobile: 576,
    tablet: 768,
    desktop: 992,
    large: 1200
  },

  // URLs da API (se precisar)
  api: {
    baseUrl: '/api',
    timeout: 10000
  },

  // 🔐 Endpoints de autenticação
  auth: {
    loginUrl: '/auth/login',
    refreshUrl: '/auth/refresh',
    logoutUrl: '/auth/logout'
  }
};

// Congela o objeto para evitar modificações acidentais
Object.freeze(Config);