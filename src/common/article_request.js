import axios from 'axios'
import BigInt from 'json-bigint'
import store from '@/stores/store'

// create an axios instance
const service = axios.create({
  baseURL: '/article',
  timeout: 10000,
  transformResponse(data) {
    if (data)
      return BigInt.parse(data)
  }
})

const isImgUpload = (config) => {
  return config.url && (config.url.indexOf('upload_picture') !== -1 || config.url.indexOf('user/photo') !== -1 || config.url.indexOf('article/import') !== -1)
}

// request interceptor
service.interceptors.request.use(
  config => {
    const accessToken = store.state.accessToken
    if (accessToken) {
      if (!isImgUpload(config)) {
        config.headers['Content-Type'] = 'application/json'
      } else {
        // FormData 上传：删除手动 Content-Type，让浏览器自动设置带 boundary 的值
        delete config.headers['Content-Type']
      }
      config.headers['accToken'] = accessToken
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// response interceptor
service.interceptors.response.use(
  response => {
    const data = response.data
    // 检查响应code字段，非200时视为错误
    if (data && data.code !== undefined && data.code !== 200) {
      return Promise.reject({ code: data.code, message: data.message || '服务器内部错误', data: data.data })
    }
    return data
  },
  error => {
    // 444 — accessToken过期，尝试刷新后重放
    if (error.response && error.response.status === 444) {
      return refreshTokenAndRetry(error.config)
    }
    return Promise.reject(error)
  }
)

// 刷新token并重放请求
function refreshTokenAndRetry(config) {
  const accessToken = store.state.accessToken
  const refreshToken = store.state.refreshToken
  if (!refreshToken) {
    store.dispatch('showLogin')
    return Promise.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
  }
  const refreshUrl = '/user/api/v1/token/refresh'
  const refreshTime = new Date().getTime()
  return axios({
    method: 'POST',
    url: refreshUrl,
    headers: {
      'Content-Type': 'application/json; charset=UTF-8',
      'accToken': accessToken,
      't': '' + refreshTime
    },
    timeout: 10000,
    data: { refreshToken: refreshToken }
  }).then(response => {
    const d = response.data
    if (d && d.code === 200 && d.data && d.data.accessToken) {
      store.dispatch('login', d.data)
      // 用新token重放原请求
      config.headers['accToken'] = d.data.accessToken
      return axios(config).then(res => res.data)
    }
    store.dispatch('logout')
    store.dispatch('showLogin')
    return Promise.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
  }).catch(() => {
    store.dispatch('logout')
    store.dispatch('showLogin')
    return Promise.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
  })
}

export default service
