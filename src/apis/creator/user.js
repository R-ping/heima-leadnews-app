import request from '@/common/article_request'

const API_USERPROFILE = '/api/v1/user/profile'
const API_HEAD = '/api/v1/user/photo'

// 获取用户个人资料
export const getUserProfile = () => {
  return request.get(API_USERPROFILE)
}

// 更新用户个人资料
export const updateUserProfile = (data) => {
  return request.patch(API_USERPROFILE, data)
}

// 更新用户头像
export const updateUserHead = (data) => {
  return request.patch(API_HEAD, data)
}