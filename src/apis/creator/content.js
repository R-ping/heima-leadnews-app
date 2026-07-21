import request from '@/common/wemedia_request'

const API_STATISTICS_NEWS = '/api/v1/statistics/news'
const API_ARTICLES_INFO = '/api/v1/news/news'
const API_ARTICLES_DELETE = '/api/v1/news/del_news'
const API_SEARCHARTICELS = '/api/v1/news/list'
const API_ARTICLES_UPDOWN = '/api/v1/news/down_or_up'

// 获取图文统计数据
export const getNewsStatistics = (params) => {
  return request.get(API_STATISTICS_NEWS, { params })
}

// 根据ID获取文章详情
export const getArticleById = (articlesId) => {
  return request.post(API_ARTICLES_INFO, { id: articlesId })
}

// 删除文章
export const deleteArticles = (data) => {
  return request.delete(API_ARTICLES_DELETE, { data })
}

// 检索文章
export const searchArticle = (params) => {
  return request.get(API_SEARCHARTICELS, { params })
}

// 文章上下架
export const upDownArticle = (data) => {
  return request.post(API_ARTICLES_UPDOWN, data)
}