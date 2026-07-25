import request from '@/common/article_request'

export const getHotArticles = (category = 'comprehensive', limit = 20) => {
  return request.get('/api/v1/hot/articles', { params: { category, limit } })
}

export const getCollectedArticles = (limit = 20) => {
  return request.get('/api/v1/hot/collected-articles', { params: { limit } })
}

export const getHotAuthors = (period = 'weekly', limit = 20) => {
  return request.get('/api/v1/hot/authors', { params: { period, limit } })
}

export const getHotMeta = (tab, category, period) => {
  return request.get('/api/v1/hot/meta', { params: { tab, category, period } })
}