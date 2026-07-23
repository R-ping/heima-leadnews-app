import request from '@/common/article_request'

/**
 * 粉丝数据 - 统计概览（KPI指标）
 */
export const getFansStatistics = (params) => {
  return request.get('/api/v1/data/fans/statistics', { params })
}

/**
 * 粉丝数据 - 趋势数据
 */
export const getFansTrend = (params) => {
  return request.get('/api/v1/data/fans/trend', { params })
}

/**
 * 粉丝数据 - 粉丝列表
 */
export const getFansList = (params) => {
  return request.get('/api/v1/data/fans/list', { params })
}

/**
 * 粉丝数据 - 关注/回关粉丝
 */
export const followFans = (userId) => {
  return request.post('/api/v1/data/fans/follow', { userId })
}

/**
 * 粉丝画像数据（info页使用）
 */
export const getFollowersPortrait = () => {
  return request.get('/api/v1/data/fans/portrait')
}

/**
 * 粉丝列表（旧版list页使用）
 */
export const getFollowers = (params) => {
  return request.get('/api/v1/data/fans/list', { params })
}

export const getFollowersAvatar = (params) => {
  return request.get('/api/v1/data/fans/avatars', { params })
}

export const changeFollowState = (data) => {
  return request.post('/api/v1/data/fans/follow', data)
}