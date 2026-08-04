import request from '@/common/reward_request'

export const getDashboard = () => {
  return request.get('/api/v1/checkin/dashboard')
}

export const doCheckIn = () => {
  return request.post('/api/v1/checkin/do')
}

export const doRetroactive = (missedDate) => {
  return request.post('/api/v1/checkin/patch', { targetDate: missedDate })
}

export const getCheckInRecords = (params) => {
  return request.get('/api/v1/checkin/milestone', { params })
}

export const getCheckInStats = () => {
  return request.get('/api/v1/checkin/stats')
}

export const getTodayStatus = () => {
  return request.get('/api/v1/checkin/today')
}

export const getCheckInTasks = () => {
  return request.get('/api/v1/checkin/tasks')
}