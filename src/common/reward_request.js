import axios from 'axios'
import store from '@/stores/store'
import tokenManager from './tokenManager'

const service = axios.create({
  baseURL: '/reward',
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const accessToken = store.state.accessToken
    if (accessToken) {
      config.headers['Content-Type'] = 'application/json'
      config.headers['accToken'] = accessToken
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const data = response.data
    if (data && data.code !== undefined && data.code !== 200) {
      return Promise.reject({ code: data.code, message: data.message || '服务器内部错误', data: data.data })
    }
    return data
  },
  error => {
    if (error.response && error.response.status === 401) {
      store.dispatch('logout')
      store.dispatch('showLogin')
      return Promise.reject(error)
    }
    if (error.response && error.response.status === 444) {
      return refreshTokenAndRetry(error.config)
    }
    return Promise.reject(error)
  }
)

function refreshTokenAndRetry(config) {
  const refreshToken = store.state.refreshToken
  if (!refreshToken) {
    store.dispatch('logout')
    store.dispatch('showLogin')
    return Promise.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
  }
  return tokenManager.refresh(function (newToken) {
    config.headers['accToken'] = newToken
    return service(config)
  })
}

export default service