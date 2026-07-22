import request from '@/common/request'

export const getUserStatistics = () => {
  return request.get('/api/v1/user/statistics')
}