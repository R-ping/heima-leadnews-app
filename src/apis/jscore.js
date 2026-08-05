import request from '@/common/article_request'

export const getJScoreOverview = () => {
  return request.get('/api/v1/jscore/overview')
}

export const getJScoreDetail = (params) => {
  return request.get('/api/v1/jscore/detail', { params })
}