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