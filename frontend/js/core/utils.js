// ========================================
// FUNÇÕES AUXILIARES
// ========================================

/**
 * Anima um valor numérico de start até end
 */
export function animateValue(element, start, end, duration = 1500) {
  const range = end - start;
  if (range === 0) {
    element.textContent = end;
    return;
  }
  
  const increment = range / (duration / 16);
  let current = start;
  const isAscending = end > start; // 🔥 Checa a direção da animação

  const timer = setInterval(() => {
    current += increment;
    
    // 🔥 Condicional ajustada para travar no alvo independente de subir ou descer
    if ((isAscending && current >= end) || (!isAscending && current <= end)) {
      element.textContent = Math.round(end);
      clearInterval(timer);
    } else {
      element.textContent = Math.round(current);
    }
  }, 16);
}

/**
 * Anima um contador até o target
 */
export function animateCounter(element, target, duration = 1000) {
  animateValue(element, 0, target, duration);
}

/**
 * Debounce - evita execuções excessivas
 */
export function debounce(func, wait = 300) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * Throttle - limita execuções
 */
export function throttle(func, limit = 300) {
  let inThrottle;
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

/**
 * Formata data para pt-BR
 */
export function formatDate(date) {
  return date.toLocaleDateString('pt-BR');
}

/**
 * Verifica se é mobile
 */
import { Config } from './config.js';

export function isMobile() {
  return window.innerWidth <= Config.breakpoints.tablet; // ✅ Acoplado ao seu padrão central
}

/**
 * Safe querySelector
 */
export function $(selector, parent = document) {
  return parent.querySelector(selector);
}

/**
 * Safe querySelectorAll
 */
export function $$(selector, parent = document) {
  return Array.from(parent.querySelectorAll(selector));
}

/**
 * Adiciona evento com cleanup
 */
export function on(element, event, handler) {
  if (element) {
    element.addEventListener(event, handler);
    return () => element.removeEventListener(event, handler);
  }
  return () => {};
}