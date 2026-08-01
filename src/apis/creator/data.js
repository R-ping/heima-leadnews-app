import request from '@/common/article_request'

/**
 * 内容数据 - 文章统计概览
 */
export const getArticleStatistics = (params) => {
  return request.get('/api/v1/data/content/statistics', { params })
}

/**
 * 内容数据 - 文章趋势数据
 */
export const getArticleTrend = (params) => {
  return request.get('/api/v1/data/content/trend', { params })
}

/**
 * 内容数据 - 文章单篇分析列表
 */
export const getArticleDetailList = (params) => {
  return request.get('/api/v1/data/content/detail', { params })
}

/**
 * 内容数据 - 专栏统计概览
 */
export const getColumnStatistics = (params) => {
  return request.get('/api/v1/data/column/statistics', { params })
}

/**
 * 内容数据 - 专栏趋势数据
 */
export const getColumnTrend = (params) => {
  return request.get('/api/v1/data/column/trend', { params })
}

/**
 * 内容数据 - 专栏单个分析列表
 */
export const getColumnDetailList = (params) => {
  return request.get('/api/v1/data/column/detail', { params })
}

/**
 * 内容数据 - 沸点统计概览
 */
export const getPinStatistics = (params) => {
  return request.get('/api/v1/data/pin/statistics', { params })
}

/**
 * 内容数据 - 沸点趋势数据
 */
export const getPinTrend = (params) => {
  return request.get('/api/v1/data/pin/trend', { params })
}

/**
 * 内容数据 - 沸点单条分析列表
 */
export const getPinDetailList = (params) => {
  return request.get('/api/v1/data/pin/detail', { params })
}