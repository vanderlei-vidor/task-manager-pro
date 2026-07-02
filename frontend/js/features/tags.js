// ========================================
// GESTÃO DE TAGS - SELECTOR E PREVIEW
// ========================================
import { Toast } from '../core/toast.js';
import { $, $$ } from '../core/utils.js';

class TagsManager {
  constructor() {
    // 💡 O constructor guarda apenas referências de estado. 
    // Elementos do DOM ficam nulos até o init() rodar na página ativa!
    this.colorPicker = null;
    this.tagPreview = null;
    this.tagPreviewDot = null;
    this.tagPreviewName = null;
    this.tagNameInput = null;
    this.colorPresets = [];
    this.tagLabels = [];
  }

  init() {
    // 🚀 Captura os elementos dinamicamente no ciclo de vida correto da página atual
    this.colorPicker = $('.color-picker');
    this.tagPreview = $('#tagPreview');
    this.tagPreviewDot = $('#tagPreviewDot');
    this.tagPreviewName = $('#tagPreviewName');
    this.tagNameInput = $('input[name="name"]');
    this.colorPresets = $$('.color-preset');
    this.tagLabels = $$('.tag-label');

    // Linha de defesa: se não houver elementos de preview ou labels na página, corta a execução
    if (!this.tagPreview && !this.tagLabels.length) return;

    console.log('🏷️ Tags Manager inicializado com segurança!');
    
    this._bindColorPicker();
    this._bindNameInput();
    this._bindColorPresets();
    this._bindTagLabels();
  }

  /**
   * Configura o color picker para atualizar o preview visual
   */
  _bindColorPicker() {
    if (!this.colorPicker || !this.tagPreviewDot) return;

    this.colorPicker.addEventListener('input', (e) => {
      this.tagPreviewDot.style.background = e.target.value;
      this._updatePreviewBorder(e.target.value);
    });
  }

  /**
   * Configura input de nome para atualizar o preview em tempo real
   */
  _bindNameInput() {
    if (!this.tagNameInput || !this.tagPreviewName) return;

    this.tagNameInput.addEventListener('input', (e) => {
      const value = e.target.value.trim();
      this.tagPreviewName.textContent = value || 'Nome da Tag';
    });
  }

  /**
   * Configura presets de cores rápidas
   */
  _bindColorPresets() {
    if (!this.colorPresets.length || !this.colorPicker) return;

    this.colorPresets.forEach(preset => {
      preset.addEventListener('click', () => {
        const color = preset.dataset.color || preset.style.background;
        
        this.colorPicker.value = this._rgbToHex(color);
        // Dispara o evento de 'input' manualmente para atualizar o preview de carona
        this.colorPicker.dispatchEvent(new Event('input'));

        // Feedback visual do preset selecionado
        this.colorPresets.forEach(p => p.classList.remove('active-preset'));
        preset.classList.add('active-preset');
      });
    });
  }

  /**
   * Configura labels de tags no modal de criação/edição de tarefas
   */
  _bindTagLabels() {
    if (!this.tagLabels.length) return;

    this.tagLabels.forEach(label => {
      const forAttr = label.getAttribute('for');
      if (!forAttr) return;

      const checkbox = document.getElementById(forAttr);
      if (!checkbox) return;

      // 🔥 Abordagem sênior: Escuta a mudança de estado REAL do checkbox,
      // deixando o clique nativo da label funcionar livremente.
      checkbox.addEventListener('change', () => {
        console.log(`✅ Tag alterada: ${checkbox.value} (Ativa: ${checkbox.checked})`);
        this._updateTagLabelStyle(label, checkbox.checked);
      });
    });
  }

  /**
   * Aplica feedback visual de micro-interação na label selecionada
   */
  _updateTagLabelStyle(label, isChecked) {
    if (isChecked) {
      label.classList.add('selected'); // Controlar via SASS fica muito mais limpo!
      label.style.transform = 'scale(1.05)';
      setTimeout(() => {
        label.style.transform = '';
      }, 150);
    } else {
      label.classList.remove('selected');
    }
  }

  /**
   * Atualiza a borda externa do distintivo de preview
   */
  _updatePreviewBorder(color) {
    const previewBadge = $('.tag-preview-badge');
    if (previewBadge) {
      previewBadge.style.borderColor = color;
    }
  }

  /**
   * Converte strings RGB de estilização do browser para formato Hexadecimal válido
   */
  _rgbToHex(color) {
    if (color.startsWith('#')) return color;

    const match = color.match(/\d+/g);
    if (!match || match.length < 3) return '#6366f1'; // Cor primária fallback

    const [r, g, b] = match.map(Number);
    return '#' + [r, g, b]
      .map(x => x.toString(16).padStart(2, '0'))
      .join('');
  }
}

export const Tags = new TagsManager();