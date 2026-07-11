// ========================================
// CAMADA DE API CENTRALIZADA
// ========================================
// Resolve três problemas que existiam antes:
//  1. Token era lido direto em localStorage.getItem('accessToken') no kanban.js
//     (sem abstração, sem ponto central para manter/limpar).
//  2. Não existia refresh-on-401: access token expirado → erro silencioso.
//  3. Não havia logout automático quando o refresh também falhava.
//
// Uso:
//   import { apiFetchWithRefresh } from '../core/api.js';
//   const res = await apiFetchWithRefresh('/api/tasks/1/status', { method: 'PUT', ... });

import { Config } from './config.js';
import { Storage, StorageKeys } from './storage.js';

// Guarda contra loops infinitos de refresh simultâneos
let _isRefreshing = false;
let _refreshPromise = null;

/**
 * Lê o access token do storage (string pura).
 */
export function getAccessToken() {
  return Storage.getToken(StorageKeys.ACCESS_TOKEN);
}

/**
 * Lê o refresh token do storage (string pura).
 */
export function getRefreshToken() {
  return Storage.getToken(StorageKeys.REFRESH_TOKEN);
}

/**
 * Atualiza ambos os tokens no storage (após refresh bem-sucedido).
 */
export function setTokens(accessToken, refreshToken) {
  Storage.setToken(StorageKeys.ACCESS_TOKEN, accessToken);
  if (refreshToken) {
    Storage.setToken(StorageKeys.REFRESH_TOKEN, refreshToken);
  }
}

/**
 * Limpa ambos os tokens (logout / sessão expirada).
 */
export function clearTokens() {
  Storage.removeToken(StorageKeys.ACCESS_TOKEN);
  Storage.removeToken(StorageKeys.REFRESH_TOKEN);
}

/**
 * Wrapper de fetch que ANEXA o Authorization header automaticamente.
 *
 * Não faz refresh — use apiFetchWithRefresh para isso.
 */
export async function apiFetch(url, options = {}) {
  const token = getAccessToken();
  const headers = { ...(options.headers || {}) };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return fetch(url, { ...options, headers });
}

/**
 * Chama o endpoint de refresh UMA VEZ, mesmo se houver chamadas simultâneas.
 * Evita race condition quando múltiplas requisições recebem 401 ao mesmo tempo.
 *
 * @returns {Promise<boolean>} true se o refresh deu certo, false caso contrário.
 */
async function _doRefresh() {
  if (_isRefreshing && _refreshPromise) {
    return _refreshPromise;
  }

  _isRefreshing = true;
  _refreshPromise = (async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      return false;
    }

    try {
      const response = await fetch(Config.auth.refreshUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });

      if (!response.ok) {
        return false;
      }

      const data = await response.json();
      setTokens(data.accessToken, data.refreshToken);
      return true;
    } catch (error) {
      console.error('Falha no refresh do token:', error);
      return false;
    } finally {
      _isRefreshing = false;
      _refreshPromise = null;
    }
  })();

  return _refreshPromise;
}

/**
 * Wrapper de fetch com REFRESH-ON-401 automático.
 *
 * Fluxo:
 *  1. Faz a requisição com o token atual.
 *  2. Se receber 401 (Unauthorized), tenta renovar via /auth/refresh.
 *  3. Se o refresh der certo, refaz a requisição original com o novo token.
 *  4. Se o refresh falhar, limpa os tokens e redireciona para /login.
 *
 * @param {string} url - URL da requisição (relativa ou absoluta).
 * @param {object} options - mesmas opções do fetch.
 */
export async function apiFetchWithRefresh(url, options = {}) {
  // Primeira tentativa
  let response = await apiFetch(url, options);

  // Se não for 401, retorna direto
  if (response.status !== 401) {
    return response;
  }

  // 401 → tenta refresh
  const refreshed = await _doRefresh();

  if (!refreshed) {
    // Refresh falhou → sessão expirada. Limpa e manda pro login.
    clearTokens();
    console.warn('Sessão expirada. Redirecionando para /login.');
    // Evita redirect em páginas públicas (ex.: já estamos no /login)
    if (window.location.pathname !== '/login') {
      window.location.href = '/login?expired=true';
    }
    return response;
  }

  // Refresh OK → refaz a requisição original com o novo token
  return apiFetch(url, options);
}
