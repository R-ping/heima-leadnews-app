import request from '@/common/article_request'

export const getBrowseHistory = (params) => {
  return request.get('/api/v1/browse-history/list', { params })
}

export const clearBrowseHistory = () => {
  return request.post('/api/v1/browse-history/clear')
}

export const reportBrowse = (data) => {
  return request.post('/api/v1/browse-history/report', data)
}