import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue2'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { existsSync } from 'node:fs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const srcDir = path.resolve(__dirname, 'src').replace(/\\/g, '/')

export default defineConfig({
  plugins: [
    vue(),
    { 
      name: 'resolve-at-alias',
      resolveId(source) {
        if (!source.startsWith('@/')) return null
        const cleanPath = srcDir + '/' + source.slice(2)
        // Return exact path if it has an extension
        if (/\.\w+$/.test(cleanPath)) return cleanPath
        // Try common extensions
        for (const ext of ['.js', '.vue', '.json', '.less', '.css']) {
          const fullPath = cleanPath + ext
          if (existsSync(fullPath)) return fullPath.replace(/\\/g, '/')
        }
        return cleanPath + '.js'
      }
    }
  ],
  server: {
    port: 9901,
    allowedHosts: [
      '195b7e5b.r40.cpolar.top',
      '.cpolar.top',
      'localhost'
    ],
    proxy: {
      '/server_85': {
        target: 'http://heima-app-java.research.itcast.cn',
        changeOrigin: true
      },
      '/article': {
        target: 'http://127.0.0.1:51601/',
        changeOrigin: true,
        bypass(req) {
          // 前端路由 /article 和 /article/:id 由 SPA 处理，不代理到后端
          if (req.url === '/article' || req.url.startsWith('/article?') || /^\/article\/\d+/.test(req.url)) {
            return req.url;
          }
        }
      },
      '/behavior': {
        target: 'http://127.0.0.1:51601/',
        changeOrigin: true
      },
      '/user': {
        target: 'http://127.0.0.1:51601/',
        changeOrigin: true
      },
      '/search': {
        target: 'http://127.0.0.1:51601/',
        changeOrigin: true
      },
      '/wemedia': {
        target: 'http://127.0.0.1:51601/',
        changeOrigin: true
      },
      '/minio-static': {
        target: 'http://127.0.0.1:9005',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/minio-static/, '/leadnews')
      }
    }
  }
})
