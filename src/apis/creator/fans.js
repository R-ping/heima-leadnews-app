import request from '@/common/wemedia_request'

const API_FANS = '/api/v1/user_fans/list'
const API_FOLLOWER_PORTRAIT = '/api/v1/user_fans/fans_portrait'
const API_FANS_AVATAR = '/api/v1/user_fans/avatar'
const API_CHANGE_FOLLOW_STATE = '/api/v1/user_fans/change_follow_state'
const API_GET_FANS_STATISTIC = '/api/v1/statistics/fans'

// 获取粉丝列表
export const getFollowers = (params) => {
  return request.get(API_FANS, { params })
}

// 获取粉丝画像数据
export const getFollowersPortrait = (params) => {
  return request.get(API_FOLLOWER_PORTRAIT, { params })
}

// 获取粉丝头像
export const getFollowersAvatar = (params) => {
  return request.get(API_FANS_AVATAR, { params })
}

// 改变粉丝关注状态
export const changeFollowState = (data) => {
  return request.post(API_CHANGE_FOLLOW_STATE, data)
}

// 获取粉丝统计数据
export const getFansStatistics = (params) => {
  return request.get(API_GET_FANS_STATISTIC, { params })
}