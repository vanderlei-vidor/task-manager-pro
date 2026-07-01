import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  build: {
    // 🚀 Joga o resultado final direto dentro dos Assets Estáticos do Spring
    outDir: resolve(__dirname, 'src/main/resources/static/dist'),
    emptyOutDir: true, // Limpa a pasta a cada build para não acumular lixo
    rollupOptions: {
      input: {
        // Seu ponto de entrada principal
        main: resolve(__dirname, 'frontend/js/main.js')
      },
      output: {
        // Mantém nomes limpos ou com hash para produção
        entryFileNames: 'js/[name].js', 
        chunkFileNames: 'js/chunks/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  }
});