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
    proxy: {
      '/server_85': {
        target: 'http://heima-app-java.research.itcast.cn',
        changeOrigin: true
      },
      '/article': {
        target: 'http://127.0.0.1:9003/',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/article/, '')
      },
      '/behavior': {
        target: 'http://127.0.0.1:9004/',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/behavior/, '')
      },
      '/user': {
        target: 'http://127.0.0.1:9005/',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/user/, '')
      },
      '/login': {
        target: 'http://127.0.0.1:9001/',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/login/, '')
      }
    }
  }
})
