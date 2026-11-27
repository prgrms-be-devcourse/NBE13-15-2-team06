import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 개발 서버를 3000번 포트로 띄우고, /api 요청은 스프링 서버(8080)로 넘긴다.
// 브라우저 입장에선 같은 주소(3000)로 요청하는 셈이라 개발 중엔 CORS 설정이 필요 없다.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
