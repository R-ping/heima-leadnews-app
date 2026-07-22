import request from '@/common/article_request'

export const getArticleList = (params) => {
  return request.get('/api/v1/article/manage/list', { params })
}

export const getArticleStatistics = () => {
  return request.get('/api/v1/article/manage/statistics')
}

export const deleteArticle = (id) => {
  return request.post('/api/v1/article/manage/delete', { id })
}

export const getDraftList = (params) => {
  return request.get('/api/v1/draft/manage/list', { params })
}

export const deleteDraft = (id) => {
  return request.post('/api/v1/draft/manage/delete', { id })
}

export const getColumnList = (params) => {
  return request.get('/api/v1/column/manage/list', { params })
}

export const getColumnStatistics = () => {
  return request.get('/api/v1/column/manage/statistics')
}

export const createColumn = (data) => {
  return request.post('/api/v1/column/manage/create', data)
}

export const updateColumn = (data) => {
  return request.post('/api/v1/column/manage/update', data)
}

export const deleteColumn = (id) => {
  return request.post('/api/v1/column/manage/delete', { id })
}

export const getPinsList = (params) => {
  return request.get('/api/v1/pins/manage/list', { params })
}

export const getPinsStatistics = () => {
  return request.get('/api/v1/pins/manage/statistics')
}

export const createPins = (data) => {
  return request.post('/api/v1/pins/manage/create', data)
}

export const deletePins = (id) => {
  return request.post('/api/v1/pins/manage/delete', { id })
}
