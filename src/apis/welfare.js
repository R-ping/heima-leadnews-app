import request from '@/common/reward_request'

export const getGoodsList = (params) => {
  return request.get('/api/v1/welfare/goods', { params })
}

export const getGoodsDetail = (goodsId) => {
  return request.get('/api/v1/welfare/goods/' + goodsId)
}

export const doExchange = (data) => {
  return request.post('/api/v1/welfare/exchange', data)
}

export const getMyExchanges = (params) => {
  return request.get('/api/v1/welfare/my-exchanges', { params })
}

export const getCommunityProps = (params) => {
  return request.get('/api/v1/welfare/community/props', { params })
}

export const exchangeCommunityProp = (propId) => {
  return request.post('/api/v1/welfare/community/exchange', { propId })
}

export const getAddressList = () => {
  return request.get('/api/v1/welfare/address')
}

export const addAddress = (data) => {
  return request.post('/api/v1/welfare/address', data)
}
