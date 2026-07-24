import request from '@/common/article_request'

export const getDashboard = () => {
  return request.get('/api/v1/checkin/dashboard')
}

export const doCheckIn = () => {
  return request.post('/api/v1/checkin/do')
}

export const doRetroactive = (missedDate) => {
  return request.post('/api/v1/checkin/retroactive', null, { params: { missedDate } })
}

export const getCheckInRecords = (params) => {
  return request.get('/api/v1/checkin/records', { params })
}

export const getCheckInStats = () => {
  return request.get('/api/v1/checkin/stats')
}

export const getCheckInTasks = () => {
  return request.get('/api/v1/checkin/tasks')
}