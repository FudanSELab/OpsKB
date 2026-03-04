import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    proxy: {
      '/knowledgebase': {
        target: 'http://127.0.0.1:8052',
        changeOrigin: true,
      },
      '/question': {
        target: 'http://127.0.0.1:8052',
        changeOrigin: true,
      },
    },
  },
});
