// ========================================
// CALENDÁRIO - NAVEGAÇÃO
// ========================================
import { $ } from '../core/utils.js';

class CalendarManager {
  constructor() {
    // 💡 Inicializa as propriedades limpas para o escopo do objeto
    this.btnPrev = null;
    this.btnNext = null;
    this.btnToday = null;
    this.mesAtual = null;
    this.anoAtual = null;
  }

  init() {
    // 🚀 Primeira linha de defesa: Se não for a página de calendário, corta a execução na hora!
    if (window.location.pathname !== '/calendario') return;

    console.log('📅 Calendário detectado!');

    // 💡 Captura os elementos do DOM de forma segura com a página carregada
    this.btnPrev = $('#btnMesAnterior');
    this.btnNext = $('#btnMesProximo');
    this.btnToday = $('#btnHoje');

    // 💡 Captura os parâmetros da URL atualizada dinamicamente
    const urlParams = new URLSearchParams(window.location.search);
    this.mesAtual = parseInt(urlParams.get('mes')) || new Date().getMonth() + 1;
    this.anoAtual = parseInt(urlParams.get('ano')) || new Date().getFullYear();

    this._bindEvents();
  }

  _navigate(mes, ano) {
    window.location.href = `/calendario?mes=${mes}&ano=${ano}`;
  }

  _bindEvents() {
    if (this.btnPrev) {
      this.btnPrev.addEventListener('click', (e) => {
        e.preventDefault();
        this.mesAtual--;
        if (this.mesAtual < 1) {
          this.mesAtual = 12;
          this.anoAtual--;
        }
        this._navigate(this.mesAtual, this.anoAtual);
      });
    }

    if (this.btnNext) {
      this.btnNext.addEventListener('click', (e) => {
        e.preventDefault();
        this.mesAtual++;
        if (this.mesAtual > 12) {
          this.mesAtual = 1;
          this.anoAtual++;
        }
        this._navigate(this.mesAtual, this.anoAtual);
      });
    }

    if (this.btnToday) {
      this.btnToday.addEventListener('click', (e) => {
        e.preventDefault();
        const hoje = new Date();
        this._navigate(hoje.getMonth() + 1, hoje.getFullYear());
      });
    }
  }
}

export const Calendar = new CalendarManager();