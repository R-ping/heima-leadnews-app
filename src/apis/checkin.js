import request from '@/common/article_request'

export const doCheckIn = () => {
  return request.get('/api/v1/checkin/do')
}

export const getCheckInRecords = (params) => {
  return request.get('/api/v1/checkin/records', { params })
}

export const getCheckInStats = () => {
  return request.get('/api/v1/checkin/stats')
}