import request from '@/common/reward_request'

export const getDashboard = () => {
  return request.get('/api/v1/lottery/dashboard')
}

export const doDraw = (type, useFree) => {
  return request.post('/api/v1/lottery/draw', { type, useFree })
}

export const claimPhysical = (data) => {
  return request.post('/api/v1/lottery/claim-physical', data)
}

export const getMyPrizes = (params) => {
  return request.get('/api/v1/lottery/my-prizes', { params })
}

export const getBroadcast = () => {
  return request.get('/api/v1/lottery/broadcast/recent')
}