import axios from 'axios'
import store from '@/stores/store'

// 全局唯一的状态标志和请求队列，解决 request.js 和 article_request.js 各自独立刷新导致并发冲突
let _refreshing = false
let _pendingRequests = []

/**
 * 统一 Token 刷新管理器
 *
 * 两个请求拦截器（request.js / article_request.js）都通过此函数刷新 token，
 * 确保同一时间只有一个刷新请求在执行，其他等待请求排队，刷新成功后统一重放。
 *
 * @param {Function} retryFn - 重放请求的函数，接收 (newToken) 作为参数，返回 Promise
 * @returns {Promise} 重放结果
 */
function refresh(retryFn) {
  const refreshToken = store.state.refreshToken

  if (!refreshToken) {
    store.dispatch('logout')
    store.dispatch('showLogin')
    return Promise.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
  }

  // 防止并发刷新：多个请求同时触发 444 时，只执行一次刷新
  if (_refreshing) {
    return new Promise(function (resolve, reject) {
      _pendingRequests.push({ resolve: resolve, reject: reject, retryFn: retryFn })
    })
  }

  _refreshing = true

  const refreshUrl = '/user/api/v1/token/refresh'
  const refreshTime = new Date().getTime()

  return axios({
    method: 'POST',
    url: refreshUrl,
    headers: {
      'Content-Type': 'application/json; charset=UTF-8',
      'accToken': store.state.accessToken,
      't': '' + refreshTime
    },
    timeout: 10000,
    data: { refreshToken: refreshToken }
  }).then(function (response) {
    const d = response.data
    if (d && d.code === 200 && d.data && d.data.accessToken) {
      // 刷新成功，存储新 token
      store.dispatch('login', d.data)
      _refreshing = false
      // 重放所有等待中的请求
      var pending = _pendingRequests.splice(0)
      pending.forEach(function (req) {
        req.retryFn(d.data.accessToken).then(req.resolve).catch(req.reject)
      })
      // 重放当前请求
      return retryFn(d.data.accessToken)
    }
    // 刷新失败（返回码非200）
    return handleRefreshFail()
  }).catch(function () {
    // 刷新失败（网络异常等）
    return handleRefreshFail()
  })
}

function handleRefreshFail() {
  _refreshing = false
  var failPending = _pendingRequests.splice(0)
  failPending.forEach(function (req) {
    req.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
  })
  store.dispatch('logout')
  store.dispatch('showLogin')
  return Promise.reject({ code: 444, errorMessage: '登录已过期，请重新登录' })
}

export default { refresh }